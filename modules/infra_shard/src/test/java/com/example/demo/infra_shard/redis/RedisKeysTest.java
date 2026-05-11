package com.example.demo.infra_shard.redis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RedisKeysTest {

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
