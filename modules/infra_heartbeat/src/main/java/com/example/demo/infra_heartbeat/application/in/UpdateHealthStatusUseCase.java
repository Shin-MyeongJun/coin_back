package com.example.demo.infra_heartbeat.application.in;

public interface UpdateHealthStatusUseCase {
    void toAlive();
    void toDead();
}
