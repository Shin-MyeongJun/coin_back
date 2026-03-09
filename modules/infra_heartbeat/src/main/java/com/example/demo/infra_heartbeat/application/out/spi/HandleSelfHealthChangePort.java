package com.example.demo.infra_heartbeat.application.out.spi;

public interface HandleSelfHealthChangePort {
    void handleDead();
    void handleAlive();
}
