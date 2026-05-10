package com.example.demo.infra_heartbeat.infrastrcuture.message.producer;

import com.example.demo.contracts.message.health.HeartBeatMessage;
import com.example.demo.infra_heartbeat.application.out.PublishHealthPort;
import com.example.demo.infra_heartbeat.infrastrcuture.message.HealthTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HeartbeatProducer implements PublishHealthPort<HeartBeatMessage> {

    private final KafkaTemplate<String, HeartBeatMessage> kafkaTemplate;

    @Override
    public void publish(HeartBeatMessage hbm) {
        kafkaTemplate.send(HealthTopics.HEARTBEAT, hbm);
    }
}
