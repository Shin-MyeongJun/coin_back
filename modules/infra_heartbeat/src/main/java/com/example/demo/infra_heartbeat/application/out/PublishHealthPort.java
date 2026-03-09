package com.example.demo.infra_heartbeat.application.out;

public interface PublishHealthPort<HEALTH> {
    void publish(HEALTH h);
}
