package com.example.demo.infra_heartbeat.application.usecase;

import com.example.demo.infra_heartbeat.application.out.spi.HandleModuleHealthChangePort;
import com.example.demo.infra_heartbeat.domain.HealthMeta;
import com.example.demo.infra_heartbeat.domain.ModuleName;
import com.example.demo.infra_heartbeat.domain.service.PeerHealthAggregator;
import com.example.demo.infra_heartbeat.infrastrcuture.cache.HealthCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class HealthCacheManagerTest {

    @Test
    void publishesAggregateAllDeadAndRecoveredTransitions() {
        RecordingModuleHealthHandler handler = new RecordingModuleHealthHandler();
        HealthCacheManager manager = new HealthCacheManager(
                List.of(),
                List.of(handler),
                0L,
                0L,
                new HealthCache(),
                new PeerHealthAggregator()
        );
        HealthMeta binance = new HealthMeta(ModuleName.INGESTION, "BINANCE", "binance-1");
        HealthMeta upbit = new HealthMeta(ModuleName.INGESTION, "UPBIT", "upbit-1");

        manager.on(binance, 1L);
        manager.on(upbit, 1L);

        manager.check();

        Assertions.assertTrue(handler.events.contains("dead:INGESTION:null"));
        Assertions.assertTrue(handler.events.contains("dead:INGESTION:BINANCE"));
        Assertions.assertTrue(handler.events.contains("dead:INGESTION:UPBIT"));

        manager.on(binance, System.currentTimeMillis());

        Assertions.assertTrue(handler.events.contains("alive:INGESTION:null"));
        Assertions.assertTrue(handler.events.contains("alive:INGESTION:BINANCE"));
        Assertions.assertFalse(handler.events.contains("alive:INGESTION:UPBIT"));
    }

    private static final class RecordingModuleHealthHandler implements HandleModuleHealthChangePort {
        private final List<String> events = new ArrayList<>();

        @Override
        public void onAllDead(ModuleName moduleName, String subType) {
            events.add("dead:" + moduleName + ":" + subType);
        }

        @Override
        public void onRecoveredFromAllDead(ModuleName moduleName, String subType) {
            events.add("alive:" + moduleName + ":" + subType);
        }
    }
}
