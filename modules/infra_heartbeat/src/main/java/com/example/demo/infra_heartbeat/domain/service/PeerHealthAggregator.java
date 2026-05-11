package com.example.demo.infra_heartbeat.domain.service;

import com.example.demo.infra_heartbeat.domain.HealthMeta;
import com.example.demo.infra_heartbeat.domain.HealthStatus;
import com.example.demo.infra_heartbeat.domain.HealthValue;
import com.example.demo.infra_heartbeat.domain.ModuleHealthKey;
import com.example.demo.infra_heartbeat.domain.ModuleHealthSummary;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PeerHealthAggregator {

    public ModuleHealthSummary summarize(Map<HealthMeta, HealthValue> snapshot, ModuleHealthKey key) {
        int aliveCount = 0;
        int deadCount = 0;
        int initializingCount = 0;

        for (Map.Entry<HealthMeta, HealthValue> entry : snapshot.entrySet()) {
            if (!matches(entry.getKey(), key)) {
                continue;
            }
            HealthStatus status = entry.getValue().status();
            if (status == HealthStatus.ALIVE) {
                aliveCount++;
            } else if (status == HealthStatus.DEAD) {
                deadCount++;
            } else if (status == HealthStatus.INITIALIZING) {
                initializingCount++;
            }
        }

        return new ModuleHealthSummary(key, aliveCount, deadCount, initializingCount);
    }

    private boolean matches(HealthMeta meta, ModuleHealthKey key) {
        if (meta.name() != key.moduleName()) {
            return false;
        }
        if (key.subType() == null) {
            return true;
        }
        return key.subType().equals(meta.subType());
    }
}
