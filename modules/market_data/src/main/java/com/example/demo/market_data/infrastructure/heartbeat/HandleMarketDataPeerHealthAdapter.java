package com.example.demo.market_data.infrastructure.heartbeat;

import com.example.demo.infra_heartbeat.application.out.spi.HandleModuleHealthChangePort;
import com.example.demo.infra_heartbeat.domain.ModuleName;
import com.example.demo.market_data.application.port.in.HandleHealthDataUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HandleMarketDataPeerHealthAdapter implements HandleModuleHealthChangePort {

    private final HandleHealthDataUseCase useCase;
    @Override
    public void onAllDead(ModuleName moduleName, String subType) {
        if (moduleName != ModuleName.INGESTION) {
            return;
        }
        if (subType == null) {
            return;
        }
        useCase.handlePeerAllDead(subType);
    }

    @Override
    public void onRecoveredFromAllDead(ModuleName moduleName, String subType) {
        if (moduleName == ModuleName.INGESTION && subType != null) {
            useCase.handlePeerRecovered(subType);
        }
    }
}
