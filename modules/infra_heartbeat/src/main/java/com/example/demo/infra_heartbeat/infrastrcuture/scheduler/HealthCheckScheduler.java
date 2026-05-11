package com.example.demo.infra_heartbeat.infrastrcuture.scheduler;

import com.example.demo.infra_heartbeat.application.in.CheckHealthCacheUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HealthCheckScheduler {
    private final CheckHealthCacheUseCase useCase;

    @Scheduled(fixedDelayString = "${app.heartbeat.check-delay-ms:1000}")
    public void check() {
        try {
            useCase.check();
        } catch (Exception e) {
            log.error("health check error: {}", e.getMessage(), e);
        }
    }
}
