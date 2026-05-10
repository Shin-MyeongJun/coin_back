package com.example.demo.infra_heartbeat.application.usecase;

import com.example.demo.infra_heartbeat.application.in.CheckHealthCacheUseCase;
import com.example.demo.infra_heartbeat.application.in.GetPeerHealthSummaryUseCase;
import com.example.demo.infra_heartbeat.application.in.OnHealthDataUseCase;
import com.example.demo.infra_heartbeat.application.out.CachingHealthPort;
import com.example.demo.infra_heartbeat.application.out.spi.HandleModuleHealthChangePort;
import com.example.demo.infra_heartbeat.application.out.spi.HandlePeerHealthChangePort;
import com.example.demo.infra_heartbeat.domain.Health;
import com.example.demo.infra_heartbeat.domain.HealthMeta;
import com.example.demo.infra_heartbeat.domain.HealthStatus;
import com.example.demo.infra_heartbeat.domain.HealthValue;
import com.example.demo.infra_heartbeat.domain.ModuleHealthKey;
import com.example.demo.infra_heartbeat.domain.ModuleHealthSummary;
import com.example.demo.infra_heartbeat.domain.ModuleName;
import com.example.demo.infra_heartbeat.domain.service.PeerHealthAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class HealthCacheManager implements OnHealthDataUseCase, CheckHealthCacheUseCase, GetPeerHealthSummaryUseCase {

    private final List<HandlePeerHealthChangePort> peerHealthHandlers;
    private final List<HandleModuleHealthChangePort> moduleHealthHandlers;
    private final long deadHoldMs;
    private final long timeoutMs;
    private final CachingHealthPort healthCache;
    private final PeerHealthAggregator aggregator;
    private final Map<ModuleHealthKey, Boolean> aggregateAllDeadStates = new ConcurrentHashMap<>();

    public HealthCacheManager(
            List<HandlePeerHealthChangePort> peerHealthHandlers,
            List<HandleModuleHealthChangePort> moduleHealthHandlers,
            @Value("${app.heartbeat.dead-hold-ms:10000}") long deadHoldMs,
            @Value("${app.heartbeat.timeout-ms:15000}") long timeoutMs,
            CachingHealthPort healthCache,
            PeerHealthAggregator aggregator
    ) {
        this.peerHealthHandlers = peerHealthHandlers;
        this.moduleHealthHandlers = moduleHealthHandlers;
        this.deadHoldMs = deadHoldMs;
        this.timeoutMs = timeoutMs;
        this.healthCache = healthCache;
        this.aggregator = aggregator;
    }

    @Override
    public void check() {
        long now = System.currentTimeMillis();
        List<HealthMeta> transitioned = new ArrayList<>();

        healthCache.snapshot().forEach((meta, val) -> {
            if (val.status() == HealthStatus.DEAD) {
                return;
            }
            if (now - val.lastUpdateTime() <= timeoutMs) {
                return;
            }

            AtomicReference<HealthStatus> previous = new AtomicReference<>();
            HealthValue result = healthCache.compute(meta, (k, cur) -> {
                previous.set(cur == null ? null : cur.status());
                if (cur == null) {
                    return null;
                }
                if (cur.status() == HealthStatus.DEAD) {
                    return cur;
                }
                if (now - cur.lastUpdateTime() <= timeoutMs) {
                    return cur;
                }
                return new HealthValue(HealthStatus.DEAD, cur.lastUpdateTime(), now + deadHoldMs);
            });

            if (result != null
                    && result.status() == HealthStatus.DEAD
                    && previous.get() != HealthStatus.DEAD) {
                transitioned.add(meta);
            }
        });

        for (HealthMeta meta : transitioned) {
            handleDead(meta);
            publishAggregateChangeIfNeeded(meta);
        }
    }

    @Override
    public void on(HealthMeta meta, Long nowMillis) {
        AtomicReference<HealthStatus> previous = new AtomicReference<>();
        HealthValue result = healthCache.compute(meta, (k, old) -> {
            previous.set(old == null ? null : old.status());
            if (old == null) {
                return new HealthValue(HealthStatus.ALIVE, nowMillis, 0L);
            }
            if (old.status() == HealthStatus.DEAD && old.inDeadHold(nowMillis)) {
                return new HealthValue(HealthStatus.DEAD, nowMillis, old.deadHoldTime());
            }
            return new HealthValue(HealthStatus.ALIVE, nowMillis, 0L);
        });

        if (result != null && result.status() == HealthStatus.ALIVE && previous.get() != HealthStatus.ALIVE) {
            handleAlive(meta);
            publishAggregateChangeIfNeeded(meta);
        }
    }

    @Override
    public void on(Health health, Long nowMillis) {
        HealthMeta meta = health.meta();
        HealthValue incoming = health.value();

        AtomicReference<HealthStatus> previous = new AtomicReference<>();
        HealthValue result = healthCache.compute(meta, (k, old) -> {
            previous.set(old == null ? null : old.status());
            if (incoming.status() == HealthStatus.DEAD) {
                return new HealthValue(HealthStatus.DEAD, nowMillis, nowMillis + deadHoldMs);
            }
            return new HealthValue(incoming.status(), nowMillis, 0L);
        });

        if (result == null || result.status() == previous.get()) {
            return;
        }
        if (result.status() == HealthStatus.DEAD) {
            handleDead(meta);
            publishAggregateChangeIfNeeded(meta);
            return;
        }
        if (result.status() == HealthStatus.ALIVE) {
            handleAlive(meta);
            publishAggregateChangeIfNeeded(meta);
        }
    }

    @Override
    public ModuleHealthSummary forModule(ModuleName moduleName) {
        return aggregator.summarize(healthCache.snapshot(), ModuleHealthKey.module(moduleName));
    }

    @Override
    public ModuleHealthSummary forSubType(ModuleName moduleName, String subType) {
        return aggregator.summarize(healthCache.snapshot(), ModuleHealthKey.subType(moduleName, subType));
    }

    Map<HealthMeta, HealthValue> getSnapshot() {
        return Map.copyOf(healthCache.snapshot());
    }

    private void handleDead(HealthMeta meta) {
        for (HandlePeerHealthChangePort uc : peerHealthHandlers) {
            uc.handleDead(meta);
        }
    }

    private void handleAlive(HealthMeta meta) {
        for (HandlePeerHealthChangePort uc : peerHealthHandlers) {
            uc.handleAlive(meta);
        }
    }

    private void publishAggregateChangeIfNeeded(HealthMeta meta) {
        publishAggregateChangeIfNeeded(ModuleHealthKey.module(meta.name()));
        if (meta.subType() != null) {
            publishAggregateChangeIfNeeded(ModuleHealthKey.subType(meta.name(), meta.subType()));
        }
    }

    private void publishAggregateChangeIfNeeded(ModuleHealthKey key) {
        ModuleHealthSummary summary = aggregator.summarize(healthCache.snapshot(), key);
        boolean allDead = summary.allDead();
        boolean wasAllDead = aggregateAllDeadStates.getOrDefault(key, false);
        if (allDead == wasAllDead) {
            return;
        }

        aggregateAllDeadStates.put(key, allDead);
        for (HandleModuleHealthChangePort handler : moduleHealthHandlers) {
            if (allDead) {
                handler.onAllDead(key.moduleName(), key.subType());
            } else {
                handler.onRecoveredFromAllDead(key.moduleName(), key.subType());
            }
        }
    }
}
