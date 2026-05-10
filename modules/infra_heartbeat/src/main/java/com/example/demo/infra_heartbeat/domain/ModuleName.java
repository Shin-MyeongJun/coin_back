package com.example.demo.infra_heartbeat.domain;

import java.util.Locale;

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

    public static ModuleName from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("module name is required");
        }
        String normalized = value.trim().replace('-', '_').toUpperCase(Locale.ROOT);
        return ModuleName.valueOf(normalized);
    }
}
