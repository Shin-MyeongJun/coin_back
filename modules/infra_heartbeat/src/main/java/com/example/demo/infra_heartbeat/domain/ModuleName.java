package com.example.demo.infra_heartbeat.domain;

public enum ModuleName {
    ANALYTICS("ANALYTICS"),
    INGESTION("INGESTION"),
    TRADING("TRADING"),
    MARKET_DATA("MARKET_DATA"),
    META_DATA("META_DATA"),
    API("API");

    private final String name;
    ModuleName(final String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
}
