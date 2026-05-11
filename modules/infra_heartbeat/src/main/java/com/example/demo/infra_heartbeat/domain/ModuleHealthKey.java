package com.example.demo.infra_heartbeat.domain;

/**
 * Key for an aggregated health view.
 * A null subType means the whole module, not only peers whose subType is null.
 */
public record ModuleHealthKey(
        ModuleName moduleName,
        String subType
) {
    public ModuleHealthKey {
        if (moduleName == null) {
            throw new IllegalArgumentException("moduleName is required");
        }
        subType = HealthMeta.normalizeSubType(subType);
    }

    public static ModuleHealthKey module(ModuleName moduleName) {
        return new ModuleHealthKey(moduleName, null);
    }

    public static ModuleHealthKey subType(ModuleName moduleName, String subType) {
        return new ModuleHealthKey(moduleName, subType);
    }
}
