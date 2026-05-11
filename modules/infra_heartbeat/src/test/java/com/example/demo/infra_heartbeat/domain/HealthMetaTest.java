package com.example.demo.infra_heartbeat.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HealthMetaTest {

    @Test
    void normalizesSubType() {
        HealthMeta meta = new HealthMeta(ModuleName.INGESTION, "binance-futures", "instance-1");

        Assertions.assertEquals("BINANCE_FUTURES", meta.subType());
    }

    @Test
    void allowsNullSubType() {
        HealthMeta meta = new HealthMeta(ModuleName.MARKET_DATA, "instance-1");

        Assertions.assertNull(meta.subType());
    }

    @Test
    void parsesKebabModuleName() {
        Assertions.assertEquals(ModuleName.MARKET_DATA, ModuleName.from("market-data"));
    }
}
