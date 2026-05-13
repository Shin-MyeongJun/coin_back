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
}
