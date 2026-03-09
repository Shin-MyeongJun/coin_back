package com.example.demo.infra_heartbeat.application.in;

public interface ConsumeHealthUseCase<HEALTH> {
    void consume(HEALTH h);
}
