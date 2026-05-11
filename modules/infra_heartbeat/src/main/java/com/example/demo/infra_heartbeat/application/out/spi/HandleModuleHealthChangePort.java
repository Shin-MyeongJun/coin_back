package com.example.demo.infra_heartbeat.application.out.spi;

import com.example.demo.infra_heartbeat.domain.ModuleName;

/**
 * Handles aggregated module health transitions.
 *
 * <p>This is different from HandlePeerHealthChangePort. Peer events are emitted
 * for one concrete instance, while this port is emitted only when the aggregate
 * group changes state. For example, one BINANCE ingestion instance becoming
 * ALIVE calls the peer port; this port calls onRecoveredFromAllDead only when
 * INGESTION:BINANCE was previously all dead and now has at least one ALIVE
 * instance.</p>
 *
 * <p>subType is nullable. null means the whole module, such as all INGESTION
 * peers across BINANCE, UPBIT, and other sources.</p>
 */
public interface HandleModuleHealthChangePort {
    void onAllDead(ModuleName moduleName, String subType);

    void onRecoveredFromAllDead(ModuleName moduleName, String subType);
}
