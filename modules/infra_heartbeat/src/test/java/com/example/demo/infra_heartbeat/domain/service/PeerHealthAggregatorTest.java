package com.example.demo.infra_heartbeat.domain.service;

import com.example.demo.infra_heartbeat.domain.HealthMeta;
import com.example.demo.infra_heartbeat.domain.HealthStatus;
import com.example.demo.infra_heartbeat.domain.HealthValue;
import com.example.demo.infra_heartbeat.domain.ModuleHealthKey;
import com.example.demo.infra_heartbeat.domain.ModuleHealthSummary;
import com.example.demo.infra_heartbeat.domain.ModuleName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class PeerHealthAggregatorTest {
    private final PeerHealthAggregator aggregator = new PeerHealthAggregator();

    @Test
    void summarizesWholeModuleAcrossSubTypes() {
        Map<HealthMeta, HealthValue> snapshot = Map.of(
                new HealthMeta(ModuleName.INGESTION, "BINANCE", "one"),
                new HealthValue(HealthStatus.DEAD, 1L, 0L),
                new HealthMeta(ModuleName.INGESTION, "UPBIT", "two"),
                new HealthValue(HealthStatus.ALIVE, 1L, 0L)
        );

        ModuleHealthSummary summary = aggregator.summarize(snapshot, ModuleHealthKey.module(ModuleName.INGESTION));

        Assertions.assertEquals(1, summary.aliveCount());
        Assertions.assertEquals(1, summary.deadCount());
        Assertions.assertFalse(summary.allDead());
    }

    @Test
    void summarizesSpecificSubType() {
        Map<HealthMeta, HealthValue> snapshot = Map.of(
                new HealthMeta(ModuleName.INGESTION, "BINANCE", "one"),
                new HealthValue(HealthStatus.DEAD, 1L, 0L),
                new HealthMeta(ModuleName.INGESTION, "UPBIT", "two"),
                new HealthValue(HealthStatus.ALIVE, 1L, 0L)
        );

        ModuleHealthSummary summary = aggregator.summarize(
                snapshot,
                ModuleHealthKey.subType(ModuleName.INGESTION, "BINANCE")
        );

        Assertions.assertEquals(0, summary.aliveCount());
        Assertions.assertEquals(1, summary.deadCount());
        Assertions.assertTrue(summary.allDead());
    }
}
