package com.example.demo.infra_heartbeat.application.usecase;

import com.example.demo.infra_heartbeat.application.in.ConsumeHealthUseCase;
import com.example.demo.infra_heartbeat.application.in.OnHealthDataUseCase;
import com.example.demo.infra_heartbeat.domain.Health;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConsumeHealthChangeService implements ConsumeHealthUseCase<Health> {
    private final OnHealthDataUseCase useCase;

    @Override
    public void consume(Health health) {
        useCase.on(health,System.currentTimeMillis());
    }
}
