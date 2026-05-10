package com.example.demo.infra_shard.redis;

public final class RedisKeys {
    private RedisKeys() {}

    private static String base(String env) {
        return "ys:" + env + ":v1";
    }

    // Latest
    public static String tickLatest(String env, Long marketCodeId) {
        return base(env) + ":tick:latest:" + marketCodeId;
    }

    public static String premiumLatest(String env, Long baseEx, Long compareEx, String symbol) {
        return base(env) + ":premium:latest:" + baseEx + ":" + compareEx + ":" + symbol;
    }

    public static String premiumDetailLatest(String env, Long baseEx, Long compareEx, String symbol) {
        return base(env) + ":premium:detail:latest:" + baseEx + ":" + compareEx + ":" + symbol;
    }

    public static String health(String env, String moduleName, String subType, String uuid) {
        String logicalSubType = (subType == null || subType.isBlank()) ? "none" : subType;
        return base(env) + ":health:peer:" + moduleName + ":" + logicalSubType + ":" + uuid;
    }




}
