package com.example.demo.infra_heartbeat.application.out.spi;

import com.example.demo.infra_heartbeat.domain.HealthMeta;

public interface HandlePeerHealthChangePort {
    void handleDead(HealthMeta meta);
    void handleAlive(HealthMeta meta);
}
