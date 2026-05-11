package com.example.demo.infra_heartbeat.application.in;

import com.example.demo.infra_heartbeat.domain.ModuleHealthSummary;
import com.example.demo.infra_heartbeat.domain.ModuleName;

public interface GetPeerHealthSummaryUseCase {
    ModuleHealthSummary forModule(ModuleName moduleName);

    ModuleHealthSummary forSubType(ModuleName moduleName, String subType);
}
