package com.example.demo.infra_heartbeat.application.usecase;

import com.example.demo.contracts.message.health.HealthChangeMessage;
import com.example.demo.infra_heartbeat.application.in.GetHealthStatusUseCase;
import com.example.demo.infra_heartbeat.application.in.UpdateHealthStatusUseCase;
import com.example.demo.infra_heartbeat.application.out.GetInstanceIdPort;
import com.example.demo.infra_heartbeat.application.out.PublishHealthPort;
import com.example.demo.infra_heartbeat.application.out.spi.HandleSelfHealthChangePort;
import com.example.demo.infra_heartbeat.domain.HealthStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HealthStatusManager implements UpdateHealthStatusUseCase, GetHealthStatusUseCase {
    private HealthStatus status;
    private final List<HandleSelfHealthChangePort> useCases;
    private final PublishHealthPort<HealthChangeMessage> publishPort;
    @Value("app.moduleName")
    private final String moduleName;
    private final GetInstanceIdPort idGetter;


    @Override
    public void toAlive() {
        if (status != HealthStatus.ALIVE) {
            status = HealthStatus.ALIVE;
            useCases.forEach(HandleSelfHealthChangePort::handleAlive);
            publishPort.publish(new HealthChangeMessage(
                    moduleName
                    ,idGetter.get()
                    ,HealthStatus.DEAD.getValue()
                    ,HealthStatus.ALIVE.getValue()
            ));
        }
    }

    @Override
    public void toDead() {
        if (status != HealthStatus.DEAD) {
            status = HealthStatus.DEAD;
            useCases.forEach(HandleSelfHealthChangePort::handleAlive);
            publishPort.publish(new HealthChangeMessage(
                    moduleName
                    ,idGetter.get()
                    ,HealthStatus.ALIVE.getValue()
                    ,HealthStatus.DEAD.getValue()

            ));
        }
    }

    @Override
    public HealthStatus get() {
        return status;
    }
}
