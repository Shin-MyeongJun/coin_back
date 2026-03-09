package com.example.demo.infra_heartbeat.application.usecase;

import com.example.demo.infra_heartbeat.application.in.CheckHealthCacheUseCase;
import com.example.demo.infra_heartbeat.application.in.OnHealthDataUseCase;
import com.example.demo.infra_heartbeat.application.out.CachingHealthPort;
import com.example.demo.infra_heartbeat.application.out.spi.HandlePeerHealthChangePort;
import com.example.demo.infra_heartbeat.domain.Health;
import com.example.demo.infra_heartbeat.domain.HealthMeta;
import com.example.demo.infra_heartbeat.domain.HealthStatus;
import com.example.demo.infra_heartbeat.domain.HealthValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class HealthCacheManager  implements OnHealthDataUseCase, CheckHealthCacheUseCase {

    private final List<HandlePeerHealthChangePort> useCases;
    private final Long DEAD_HOLD = 10_000L;
    private final Long TIMEOUT = 1_000L;
    private final CachingHealthPort healthCache;

    @Override
    public void check() {
        long now = System.currentTimeMillis();
        List<HealthMeta> transitioned = new java.util.ArrayList<>();

        healthCache.snapshot().forEach((meta, val) -> {
            if (val.status() == HealthStatus.DEAD) return;
            if (now - val.lastUpdateTime() <= TIMEOUT) return;

            HealthValue result = healthCache.compute(meta, (k, cur) -> {
                if (cur == null) return null;
                if (cur.status() == HealthStatus.DEAD) return cur;
                if (now - cur.lastUpdateTime() <= TIMEOUT) return cur;

                return new HealthValue(HealthStatus.DEAD, cur.lastUpdateTime(), now + DEAD_HOLD);
            });

            if (result != null && result.status() == HealthStatus.DEAD) {
                transitioned.add(meta);
            }
        });

        // side-effect는 밖에서
        for (HealthMeta meta : transitioned) {
            for (HandlePeerHealthChangePort uc : useCases) {
                uc.handleDead(meta); // 시그니처 맞춰 수정
            }
        }
    }

    @Override
    public void on(HealthMeta meta, Long nowMillis)  {
        healthCache.compute(meta, (k, old) -> {
            if (old == null) {
                // 처음 들어온 heartbeat는 ALIVE로
                return new HealthValue(HealthStatus.ALIVE, nowMillis, 0L);
            }

            // DEAD 유지 시간 중이면 승격 금지, lastSeen만 갱신
            if (old.status() == HealthStatus.DEAD && old.inDeadHold(nowMillis)) {
                return new HealthValue(HealthStatus.DEAD, nowMillis, old.deadHoldTime());
            }

            // 홀드가 끝났거나 기존이 ALIVE면 ALIVE 강제
            return new HealthValue(HealthStatus.ALIVE, nowMillis, 0L);
        });
    }

    @Override
    public void on(Health health, Long nowMillis) {
        HealthMeta meta = health.meta();
        HealthValue incoming = health.value(); // status 포함한다고 가정

        healthCache.compute(meta, (k, old) ->{
            if (incoming.status() == HealthStatus.DEAD) {
                return new HealthValue(HealthStatus.DEAD,nowMillis, nowMillis + DEAD_HOLD);
            }
            return new HealthValue(incoming.status(),nowMillis, nowMillis + DEAD_HOLD);
        });
    }


    Map<HealthMeta, HealthValue> getSnapshot() {
        return Map.copyOf(healthCache.snapshot());
    }
}
