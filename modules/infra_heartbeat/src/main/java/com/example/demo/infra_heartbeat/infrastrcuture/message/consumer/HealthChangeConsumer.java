package com.example.demo.infra_heartbeat.infrastrcuture.message.consumer;

import com.example.demo.contracts.message.health.HealthChangeMessage;
import com.example.demo.infra_heartbeat.application.in.ConsumeHealthUseCase;
import com.example.demo.infra_heartbeat.domain.Health;
import com.example.demo.infra_heartbeat.infrastrcuture.config.HeartbeatCheckProperties;
import com.example.demo.infra_shard.messaging.consumer.KafkaDomainConsumer;
import com.example.demo.infra_shard.messaging.mapper.MessageToDomain;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HealthChangeConsumer extends KafkaDomainConsumer<Health, HealthChangeMessage> {

    private final String moduleAlternation;
    private final ConsumeHealthUseCase<Health> useCase;

    public HealthChangeConsumer(MessageToDomain<HealthChangeMessage,Health> mapper, HeartbeatCheckProperties properties, ConsumeHealthUseCase<Health>  useCase) {
        super(mapper);
        moduleAlternation = properties.modules().stream()
                .map(Pattern::quote)                 // 정규식 안전 처리
                .collect(Collectors.joining("|"));
        this.useCase = useCase;
    }

    @KafkaListener(
            containerFactory = "heartbeatKafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, HealthChangeMessage> record) {
        useCase.consume(toDomain(parse(record)));
    }
}
