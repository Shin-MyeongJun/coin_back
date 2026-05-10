package com.example.demo.infra_heartbeat.infrastrcuture.message.consumer;

import com.example.demo.contracts.message.health.HealthChangeMessage;
import com.example.demo.infra_heartbeat.application.in.ConsumeHealthUseCase;
import com.example.demo.infra_heartbeat.domain.Health;
import com.example.demo.infra_heartbeat.infrastrcuture.message.HealthTopics;
import com.example.demo.infra_shard.messaging.consumer.KafkaDomainConsumer;
import com.example.demo.infra_shard.messaging.mapper.MessageToDomain;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class HealthChangeConsumer extends KafkaDomainConsumer<Health, HealthChangeMessage> {

    private final ConsumeHealthUseCase<Health> useCase;

    public HealthChangeConsumer(
            MessageToDomain<HealthChangeMessage, Health> mapper,
            ConsumeHealthUseCase<Health> useCase
    ) {
        super(mapper);
        this.useCase = useCase;
    }

    @KafkaListener(
            topics = HealthTopics.HEALTH_CHANGE,
            containerFactory = "healthChangeKafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, HealthChangeMessage> record) {
        useCase.consume(toDomain(parse(record)));
    }
}
