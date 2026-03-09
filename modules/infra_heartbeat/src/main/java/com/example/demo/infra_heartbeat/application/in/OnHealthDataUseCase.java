package com.example.demo.infra_heartbeat.application.in;

import com.example.demo.infra_heartbeat.domain.Health;
import com.example.demo.infra_heartbeat.domain.HealthMeta;

public interface OnHealthDataUseCase {
    void on(Health health , Long nowMillis);
    void on(HealthMeta health , Long nowMillis);
}
