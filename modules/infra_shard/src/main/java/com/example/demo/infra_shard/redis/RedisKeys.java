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

    // Candle state (tf 필수)
    public static String tickCandleState(String env, Long marketCodeId, String tf) {
        return base(env) + ":tick:candle:state:" + marketCodeId+ ":"+ tf;
    }

    public static String premiumCandleState(String env, Long baseEx, Long compareEx, String symbol, String tf) {
        return base(env) + ":premium:candle:state:" + baseEx + ":" + compareEx + ":" + symbol+ ":"+ tf ;
    }

    public static String premiumDetailCandleState(String env, Long baseEx, Long compareEx, String symbol, String tf) {
        return base(env) + ":premium:detail:candle:state:" + baseEx + ":" + compareEx + ":" + symbol+ ":" + tf;
    }

    // Indicator state (tf + type 필수)
    public static String tickIndicatorState(String env, Long marketCodeId,String tf,String type,String period) {
        return base(env) + ":tick:indicator:state:" + marketCodeId+ ":" + tf+ ":"  + type+ ":" + period;
    }

    public static String premiumIndicatorState(String env, Long baseEx, Long compareEx, String symbol , String tf,String type,String period) {
        return base(env) + ":premium:indicator:state:" + baseEx + ":" + compareEx + ":" + symbol+ ":"+ tf +":"+ type+ ":" + period;
    }


}
