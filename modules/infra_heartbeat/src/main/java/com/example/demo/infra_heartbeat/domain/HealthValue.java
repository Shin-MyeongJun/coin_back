package com.example.demo.infra_heartbeat.domain;

public record HealthValue(
    HealthStatus status,
    Long lastUpdateTime,
    Long deadHoldTime
){
    public boolean inDeadHold(long nowMillis) {
        return nowMillis < deadHoldTime;
    }
}
