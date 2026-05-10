package com.example.demo.infra_heartbeat.application.usecase;

import com.example.demo.contracts.message.health.HealthChangeMessage;
import com.example.demo.infra_heartbeat.application.in.GetHealthStatusUseCase;
import com.example.demo.infra_heartbeat.application.in.UpdateHealthStatusUseCase;
import com.example.demo.infra_heartbeat.application.out.GetInstanceIdPort;
import com.example.demo.infra_heartbeat.application.out.PublishHealthPort;
import com.example.demo.infra_heartbeat.application.out.spi.HandleSelfHealthChangePort;
import com.example.demo.infra_heartbeat.domain.HealthMeta;
import com.example.demo.infra_heartbeat.domain.HealthStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HealthStatusManager implements UpdateHealthStatusUseCase, GetHealthStatusUseCase {
    private HealthStatus status = HealthStatus.INITIALIZING;
    private final List<HandleSelfHealthChangePort> useCases;
    private final PublishHealthPort<HealthChangeMessage> publishPort;
    private final String moduleName;
    private final String subType;
    private final GetInstanceIdPort idGetter;

    public HealthStatusManager(
            List<HandleSelfHealthChangePort> useCases,
            PublishHealthPort<HealthChangeMessage> publishPort,
            @Value("${app.moduleName}") String moduleName,
            @Value("${app.heartbeat.sub-type:${app.subType:}}") String subType,
            GetInstanceIdPort idGetter
    ) {
        this.useCases = useCases;
        this.publishPort = publishPort;
        this.moduleName = moduleName;
        this.subType = HealthMeta.normalizeSubType(subType);
        this.idGetter = idGetter;
    }

    @Override
    public void toAlive() {
        if (status != HealthStatus.ALIVE) {
            HealthStatus previous = status;
            status = HealthStatus.ALIVE;
            useCases.forEach(HandleSelfHealthChangePort::handleAlive);
            publishPort.publish(new HealthChangeMessage(
                    moduleName,
                    subType,
                    idGetter.get(),
                    previous.getValue(),
                    HealthStatus.ALIVE.getValue()
            ));
        }
    }

    @Override
    public void toDead() {
        if (status != HealthStatus.DEAD) {
            HealthStatus previous = status;
            status = HealthStatus.DEAD;
            useCases.forEach(HandleSelfHealthChangePort::handleDead);
            publishPort.publish(new HealthChangeMessage(
                    moduleName,
                    subType,
                    idGetter.get(),
                    previous.getValue(),
                    HealthStatus.DEAD.getValue()
            ));
        }
    }

    @Override
    public HealthStatus get() {
        return status;
    }
}
