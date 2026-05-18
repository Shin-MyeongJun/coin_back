package com.example.demo.infra_shard.redis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RedisKeysTest {

    @Test
    void createsTickCandleStateKey() {
        String key = RedisKeys.tickCandleState("local", 3, "1m");

        Assertions.assertEquals("ys:local:v1:tick:candle:state:3:1m", key);
    }

    @Test
    void createsPremiumCandleStateKey() {
        String key = RedisKeys.premiumCandleState("local", 3, "1m");

        Assertions.assertEquals("ys:local:v1:premium:candle:state:3:1m", key);
    }

    @Test
    void createsPremiumDetailCandleStateKey() {
        String key = RedisKeys.premiumDetailCandleState("local", 3, "1m");

        Assertions.assertEquals("ys:local:v1:premium:detail:candle:state:3:1m", key);
    }

    @Test
    void createsTickIndicatorStateKey() {
        String key = RedisKeys.tickIndicatorState("local", 3, "1m");

        Assertions.assertEquals("ys:local:v1:tick:indicator:state:3:1m", key);
    }

    @Test
    void createsPremiumIndicatorStateKey() {
        String key = RedisKeys.premiumIndicatorState("local", 3, "1m");

        Assertions.assertEquals("ys:local:v1:premium:indicator:state:3:1m", key);
    }

    @Test
    void createsHealthKeyWithSubType() {
        String key = RedisKeys.health("local", "INGESTION", "BINANCE", "instance-1");

        Assertions.assertEquals("ys:local:v1:health:peer:INGESTION:BINANCE:instance-1", key);
    }

    @Test
    void createsHealthKeyWithNoneSubType() {
        String key = RedisKeys.health("local", "MARKET_DATA", null, "instance-1");

        Assertions.assertEquals("ys:local:v1:health:peer:MARKET_DATA:none:instance-1", key);
    }

    // ------------------------------------------------------------
    // Auth / API key / Rate limit / Alert — new keys
    // ------------------------------------------------------------

    @Test
    void createsAuthRefreshKey() {
        String key = RedisKeys.authRefresh("test", "acc-123", "jti-abc");

        Assertions.assertEquals("ys:test:v1:auth:refresh:acc-123:jti-abc", key);
    }

    @Test
    void authRefreshRejectsBlankParams() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> RedisKeys.authRefresh("test", null, "jti"));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> RedisKeys.authRefresh("test", "  ", "jti"));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> RedisKeys.authRefresh("test", "acc", null));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> RedisKeys.authRefresh("test", "acc", ""));
    }

    @Test
    void createsAuthAccessBlacklistKey() {
        String key = RedisKeys.authAccessBlacklist("test", "jti-xyz");

        Assertions.assertEquals("ys:test:v1:auth:blacklist:access:jti-xyz", key);
    }

    @Test
    void authAccessBlacklistRejectsBlankParams() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> RedisKeys.authAccessBlacklist("test", null));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> RedisKeys.authAccessBlacklist("test", " "));
    }

    @Test
    void createsApiKeyLookupKey() {
        String key = RedisKeys.apiKeyLookup("test", "ak_live_aBcD");

        Assertions.assertEquals("ys:test:v1:apikey:lookup:ak_live_aBcD", key);
    }

    @Test
    void apiKeyLookupRejectsBlankParams() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> RedisKeys.apiKeyLookup("test", null));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> RedisKeys.apiKeyLookup("test", ""));
    }

    @Test
    void createsRateLimitApiKeyKey() {
        String key = RedisKeys.rateLimitApiKey("test", "apikey-1", "1m");

        Assertions.assertEquals("ys:test:v1:ratelimit:apikey:apikey-1:1m", key);
    }

    @Test
    void rateLimitApiKeyRejectsBlankParams() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> RedisKeys.rateLimitApiKey("test", null, "1m"));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> RedisKeys.rateLimitApiKey("test", "apikey", null));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> RedisKeys.rateLimitApiKey("test", "apikey", ""));
    }

    @Test
    void createsRateLimitAccountKey() {
        String key = RedisKeys.rateLimitAccount("test", "acc-1", "1h");

        Assertions.assertEquals("ys:test:v1:ratelimit:account:acc-1:1h", key);
    }

    @Test
    void rateLimitAccountRejectsBlankParams() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> RedisKeys.rateLimitAccount("test", null, "1h"));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> RedisKeys.rateLimitAccount("test", "acc", null));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> RedisKeys.rateLimitAccount("test", " ", "1h"));
    }

    @Test
    void createsAlertCooldownKey() {
        String key = RedisKeys.alertCooldown("test", "rule-1");

        Assertions.assertEquals("ys:test:v1:alert:cooldown:rule-1", key);
    }

    @Test
    void alertCooldownRejectsBlankParams() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> RedisKeys.alertCooldown("test", null));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> RedisKeys.alertCooldown("test", " "));
    }

    @Test
    void createsAlertStateKey() {
        String key = RedisKeys.alertState("test", "rule-1");

        Assertions.assertEquals("ys:test:v1:alert:state:rule-1", key);
    }

    @Test
    void alertStateRejectsBlankParams() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> RedisKeys.alertState("test", null));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> RedisKeys.alertState("test", ""));
    }

    @Test
    void envSubstitutionAppliesToNewKeys() {
        // env="test" 가 prefix 자리에 정확히 치환되는지 확인
        Assertions.assertTrue(RedisKeys.authRefresh("test", "a", "b").startsWith("ys:test:v1:"));
        Assertions.assertTrue(RedisKeys.authAccessBlacklist("test", "j").startsWith("ys:test:v1:"));
        Assertions.assertTrue(RedisKeys.apiKeyLookup("test", "p").startsWith("ys:test:v1:"));
        Assertions.assertTrue(RedisKeys.rateLimitApiKey("test", "k", "1m").startsWith("ys:test:v1:"));
        Assertions.assertTrue(RedisKeys.rateLimitAccount("test", "a", "1m").startsWith("ys:test:v1:"));
        Assertions.assertTrue(RedisKeys.alertCooldown("test", "r").startsWith("ys:test:v1:"));
        Assertions.assertTrue(RedisKeys.alertState("test", "r").startsWith("ys:test:v1:"));
        Assertions.assertTrue(RedisKeys.rateLimitIp("test", "1.2.3.4", "m1").startsWith("ys:test:v1:"));
        Assertions.assertTrue(RedisKeys.sseTicket("test", "tok").startsWith("ys:test:v1:"));
    }

    @Test
    void createsRateLimitIpKey() {
        String key = RedisKeys.rateLimitIp("test", "203.0.113.5", "m17560");

        Assertions.assertEquals("ys:test:v1:ratelimit:ip:203.0.113.5:m17560", key);
    }

    @Test
    void rateLimitIpRejectsBlankParams() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> RedisKeys.rateLimitIp("test", null, "m1"));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> RedisKeys.rateLimitIp("test", "1.2.3.4", " "));
    }

    @Test
    void createsSseTicketKey() {
        String key = RedisKeys.sseTicket("test", "abc-xyz");

        Assertions.assertEquals("ys:test:v1:sse:ticket:abc-xyz", key);
    }

    @Test
    void sseTicketRejectsBlankParams() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> RedisKeys.sseTicket("test", null));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> RedisKeys.sseTicket("test", ""));
    }
}
