package com.example.demo.infra_heartbeat.infrastrcuture.message.producer;

import com.example.demo.contracts.message.health.HealthChangeMessage;
import com.example.demo.infra_heartbeat.application.out.PublishHealthPort;
import com.example.demo.infra_heartbeat.infrastrcuture.message.HealthTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HealthChangeProducer implements PublishHealthPort<HealthChangeMessage> {

    private final KafkaTemplate<String, HealthChangeMessage> kafkaTemplate;

    @Override
    public void publish(HealthChangeMessage hcm) {
        kafkaTemplate.send(HealthTopics.HEALTH_CHANGE, hcm);
    }
}
