package com.example.demo.infra_heartbeat.infrastrcuture.message.producer;


import com.example.demo.contracts.message.health.HealthChangeMessage;
import com.example.demo.infra_heartbeat.application.out.PublishHealthPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HealthChangeProducer implements PublishHealthPort<HealthChangeMessage> {

    private final KafkaTemplate<String, HealthChangeMessage> kafkaTemplate;
    @Value("${app.moduleName}")
    private String moduleName;

    @Override
    public void publish(HealthChangeMessage hcm) {
        kafkaTemplate.send("%s.health-change".formatted(moduleName),hcm);
    }
}
