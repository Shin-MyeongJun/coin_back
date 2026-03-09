package com.example.demo.infra_heartbeat.application.in;

import com.example.demo.infra_heartbeat.domain.HealthStatus;

public interface GetHealthStatusUseCase {
    HealthStatus get();
}
