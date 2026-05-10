package com.example.demo.infra_heartbeat.infrastrcuture.scheduler;

import com.example.demo.contracts.message.health.HeartBeatMessage;
import com.example.demo.infra_heartbeat.application.out.GetInstanceIdPort;
import com.example.demo.infra_heartbeat.application.out.PublishHealthPort;
import com.example.demo.infra_heartbeat.domain.HealthMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HeatBeatScheduler {
    private final PublishHealthPort<HeartBeatMessage> publishHealthPort;
    private final GetInstanceIdPort getIdPort;
    private final String moduleName;
    private final String subType;

    public HeatBeatScheduler(
            PublishHealthPort<HeartBeatMessage> publishHealthPort,
            GetInstanceIdPort getIdPort,
            @Value("${app.moduleName}") String moduleName,
            @Value("${app.heartbeat.sub-type:${app.subType:}}") String subType
    ) {
        this.publishHealthPort = publishHealthPort;
        this.getIdPort = getIdPort;
        this.moduleName = moduleName;
        this.subType = HealthMeta.normalizeSubType(subType);
    }

    @Scheduled(fixedDelayString = "${app.heartbeat.fixed-delay-ms:5000}")
    public void heartBeat() {
        try {
            publishHealthPort.publish(
                    new HeartBeatMessage(
                            moduleName,
                            subType,
                            getIdPort.get()
                    )
            );
        } catch (Exception e) {
            log.error("heartbeat producer error: {}", e.getMessage(), e);
        }
    }
}
