package com.example.demo.infra_heartbeat.domain;

public enum HealthStatus {
    DEAD("DEAD"),
    ALIVE("ALIVE"),
    INITIALIZING("INITIALIZING");

    private final String value;
    private HealthStatus(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
