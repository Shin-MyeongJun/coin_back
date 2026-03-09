package com.example.demo.infra_heartbeat.infrastrcuture.scheduler;

import com.example.demo.contracts.message.health.HeartBeatMessage;
import com.example.demo.infra_heartbeat.application.out.GetInstanceIdPort;
import com.example.demo.infra_heartbeat.application.out.PublishHealthPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HeatBeatScheduler {
   private final PublishHealthPort<HeartBeatMessage> publishHealthPort;
   private final GetInstanceIdPort getIdPort;
   private  final  String moduleName;

    public HeatBeatScheduler(
            PublishHealthPort<HeartBeatMessage> publishHealthPort,
            GetInstanceIdPort getIdPort,
            @Value("${app.moduleName}") String moduleName
    ) {
        this.publishHealthPort = publishHealthPort;
        this.getIdPort = getIdPort;
        this.moduleName = moduleName;

    }

   @Scheduled(fixedDelay=1000)
   public void heartBeat() {
       try {
           publishHealthPort.publish(
                   new HeartBeatMessage(
                           moduleName,
                           getIdPort.get()
                   )
           );
       } catch (Exception e) {
           log.error("heartbeat producer error: %s".formatted(e.getMessage()));
       }
    }


}
