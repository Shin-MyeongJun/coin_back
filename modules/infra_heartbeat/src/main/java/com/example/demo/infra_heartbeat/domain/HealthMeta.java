package com.example.demo.infra_heartbeat.domain;

public record HealthMeta(
        ModuleName name,
        String uuid
) {
}
