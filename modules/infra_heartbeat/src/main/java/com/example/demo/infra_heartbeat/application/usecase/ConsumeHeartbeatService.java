package com.example.demo.infra_heartbeat.application.usecase;

import com.example.demo.infra_heartbeat.application.in.ConsumeHealthUseCase;
import com.example.demo.infra_heartbeat.application.in.OnHealthDataUseCase;
import com.example.demo.infra_heartbeat.domain.HealthMeta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class ConsumeHeartbeatService implements ConsumeHealthUseCase<HealthMeta> {

    private OnHealthDataUseCase useCase;

    @Override
    public void consume(HealthMeta meta) {
        useCase.on(meta,System.currentTimeMillis());
    }
}
