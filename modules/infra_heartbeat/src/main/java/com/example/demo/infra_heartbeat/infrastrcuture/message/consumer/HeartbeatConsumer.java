package com.example.demo.infra_heartbeat.infrastrcuture.message.consumer;

import com.example.demo.contracts.message.health.HeartBeatMessage;
import com.example.demo.infra_heartbeat.application.in.ConsumeHealthUseCase;
import com.example.demo.infra_heartbeat.domain.HealthMeta;
import com.example.demo.infra_heartbeat.infrastrcuture.message.HealthTopics;
import com.example.demo.infra_shard.messaging.consumer.KafkaDomainConsumer;
import com.example.demo.infra_shard.messaging.mapper.MessageToDomain;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class HeartbeatConsumer extends KafkaDomainConsumer<HealthMeta, HeartBeatMessage> {

    private final ConsumeHealthUseCase<HealthMeta> useCase;

    public HeartbeatConsumer(
            MessageToDomain<HeartBeatMessage, HealthMeta> mapper,
            ConsumeHealthUseCase<HealthMeta> useCase
    ) {
        super(mapper);
        this.useCase = useCase;
    }

    @KafkaListener(
            topics = HealthTopics.HEARTBEAT,
            containerFactory = "heartbeatKafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, HeartBeatMessage> record) {
        useCase.consume(toDomain(parse(record)));
    }
}
