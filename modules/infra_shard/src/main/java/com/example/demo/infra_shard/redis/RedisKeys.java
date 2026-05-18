package com.example.demo.infra_shard.redis;

public final class RedisKeys {
    private RedisKeys() {}

    private static String base(String env) {
        return "ys:" + env + ":v1";
    }

    private static String requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be null or blank");
        }
        return value;
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

    public static String tickCandleState(String env, int partitionId, String tf) {
        return base(env) + ":tick:candle:state:" + partitionId + ":" + tf;
    }

    public static String premiumCandleState(String env, int partitionId, String tf) {
        return base(env) + ":premium:candle:state:" + partitionId + ":" + tf;
    }

    public static String premiumDetailCandleState(String env, int partitionId, String tf) {
        return base(env) + ":premium:detail:candle:state:" + partitionId + ":" + tf;
    }

    public static String tickIndicatorState(String env, int partitionId, String tf) {
        return base(env) + ":tick:indicator:state:" + partitionId + ":" + tf;
    }

    public static String premiumIndicatorState(String env, int partitionId, String tf) {
        return base(env) + ":premium:indicator:state:" + partitionId + ":" + tf;
    }

    public static String health(String env, String moduleName, String subType, String uuid) {
        String logicalSubType = (subType == null || subType.isBlank()) ? "none" : subType;
        return base(env) + ":health:peer:" + moduleName + ":" + logicalSubType + ":" + uuid;
    }

    /**
     * Refresh token 저장 키.
     * Format: {@code ys:{env}:v1:auth:refresh:{accountId}:{jti}}
     * TTL 가이드: refresh token 만료 시각과 동일하게 설정(예: 14d).
     * jti(rotation marker)당 단일 키를 유지해 재사용 탐지를 가능하게 한다.
     */
    public static String authRefresh(String env, String accountId, String jti) {
        return base(env)
                + ":auth:refresh:"
                + requireNonBlank(accountId, "accountId")
                + ":"
                + requireNonBlank(jti, "jti");
    }

    /**
     * Access token 블랙리스트 키(로그아웃/강제 무효화 시 등록).
     * Format: {@code ys:{env}:v1:auth:blacklist:access:{jti}}
     * TTL 가이드: 해당 access token의 잔여 만료 시간(보통 ≤ 15m).
     */
    public static String authAccessBlacklist(String env, String jti) {
        return base(env) + ":auth:blacklist:access:" + requireNonBlank(jti, "jti");
    }

    /**
     * API key 조회용 lookup 키(prefix → apiKeyId 매핑 등).
     * Format: {@code ys:{env}:v1:apikey:lookup:{prefix}}
     * TTL 가이드: 영구 또는 long(예: 24h) 후 lazy refresh. 키 회전 시 무효화 필요.
     */
    public static String apiKeyLookup(String env, String prefix) {
        return base(env) + ":apikey:lookup:" + requireNonBlank(prefix, "prefix");
    }

    /**
     * API key 기준 rate limit 버킷 키.
     * Format: {@code ys:{env}:v1:ratelimit:apikey:{apiKeyId}:{bucket}}
     * TTL 가이드: bucket window 길이(예: 1s/1m/1h)에 맞춰 설정.
     */
    public static String rateLimitApiKey(String env, String apiKeyId, String bucket) {
        return base(env)
                + ":ratelimit:apikey:"
                + requireNonBlank(apiKeyId, "apiKeyId")
                + ":"
                + requireNonBlank(bucket, "bucket");
    }

    /**
     * Account 기준 rate limit 버킷 키.
     * Format: {@code ys:{env}:v1:ratelimit:account:{accountId}:{bucket}}
     * TTL 가이드: bucket window 길이에 맞춰 설정(예: 1m/1h/1d).
     */
    public static String rateLimitAccount(String env, String accountId, String bucket) {
        return base(env)
                + ":ratelimit:account:"
                + requireNonBlank(accountId, "accountId")
                + ":"
                + requireNonBlank(bucket, "bucket");
    }

    /**
     * Alert rule 쿨다운 키(같은 룰이 짧은 시간 내 재발화되지 않도록).
     * Format: {@code ys:{env}:v1:alert:cooldown:{ruleId}}
     * TTL 가이드: 룰에 설정된 cooldown 기간(예: 5m).
     */
    public static String alertCooldown(String env, String ruleId) {
        return base(env) + ":alert:cooldown:" + requireNonBlank(ruleId, "ruleId");
    }

    public static String alertCooldown(String env, long ruleId) {
        return base(env) + ":alert:cooldown:" + ruleId;
    }

    /**
     * Alert rule 평가 상태 캐시(직전 observed value/방향 등 hysteresis 판단용).
     * Format: {@code ys:{env}:v1:alert:state:{ruleId}}
     * TTL 가이드: 영구 또는 long(예: 7d) — 룰 비활성/삭제 시 명시적 삭제 필요.
     */
    public static String alertState(String env, String ruleId) {
        return base(env) + ":alert:state:" + requireNonBlank(ruleId, "ruleId");
    }

    /**
     * IP 기반 rate limit 버킷 키(익명/anonymous 요청 fallback).
     * Format: {@code ys:{env}:v1:ratelimit:ip:{ip}:{bucket}}
     * TTL 가이드: bucket window 길이에 맞춰 설정(예: 1m).
     */
    public static String rateLimitIp(String env, String ip, String bucket) {
        return base(env)
                + ":ratelimit:ip:"
                + requireNonBlank(ip, "ip")
                + ":"
                + requireNonBlank(bucket, "bucket");
    }

    /**
     * SSE 1회용 ticket 키(브라우저 EventSource 헤더 제약 회피용).
     * Format: {@code ys:{env}:v1:sse:ticket:{token}}
     * TTL 가이드: 짧게(예: 60s). GETDEL로 1회 소비.
     */
    public static String sseTicket(String env, String token) {
        return base(env) + ":sse:ticket:" + requireNonBlank(token, "token");
    }
}
