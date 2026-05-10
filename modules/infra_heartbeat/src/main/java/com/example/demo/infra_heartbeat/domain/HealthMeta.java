package com.example.demo.infra_heartbeat.domain;

import java.util.Locale;

/**
 * subType is nullable. Use it when one module has multiple logical sources,
 * such as INGESTION with BINANCE or UPBIT.
 */
public record HealthMeta(
        ModuleName name,
        String subType,
        String uuid
) {
    public HealthMeta {
        if (name == null) {
            throw new IllegalArgumentException("name is required");
        }
        subType = normalizeSubType(subType);
    }

    public HealthMeta(ModuleName name, String uuid) {
        this(name, null, uuid);
    }

    public static String normalizeSubType(String subType) {
        if (subType == null || subType.isBlank()) {
            return null;
        }
        return subType.trim().replace('-', '_').toUpperCase(Locale.ROOT);
    }
}
