package com.example.demo.infra_heartbeat.infrastrcuture.message.consumer;


import com.example.demo.contracts.message.health.HeartBeatMessage;
import com.example.demo.infra_heartbeat.application.in.ConsumeHealthUseCase;
import com.example.demo.infra_heartbeat.domain.HealthMeta;
import com.example.demo.infra_heartbeat.infrastrcuture.config.HeartbeatCheckProperties;
import com.example.demo.infra_shard.messaging.consumer.KafkaDomainConsumer;
import com.example.demo.infra_shard.messaging.mapper.MessageToDomain;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class HeartbeatConsumer extends KafkaDomainConsumer<HealthMeta, HeartBeatMessage> {

    private final String moduleAlternation;
    private final ConsumeHealthUseCase<HealthMeta> useCase;

    public HeartbeatConsumer(MessageToDomain<HeartBeatMessage,HealthMeta> mapper, HeartbeatCheckProperties properties, ConsumeHealthUseCase<HealthMeta>  useCase) {
        super(mapper);
        moduleAlternation = properties.modules().stream()
                .map(Pattern::quote)                 // 정규식 안전 처리
                .collect(Collectors.joining("|"));
        this.useCase = useCase;
    }

    @KafkaListener(
            containerFactory = "heartbeatKafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, HeartBeatMessage> record) {
        useCase.consume(toDomain(parse(record)));
    }
}
