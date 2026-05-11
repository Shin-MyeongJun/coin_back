package com.example.demo.infra_heartbeat.domain;

public record ModuleHealthSummary(
        ModuleHealthKey key,
        int aliveCount,
        int deadCount,
        int initializingCount
) {
    public int totalCount() {
        return aliveCount + deadCount + initializingCount;
    }

    public boolean allDead() {
        return totalCount() > 0 && aliveCount == 0;
    }

    public boolean available() {
        return aliveCount > 0;
    }
}
