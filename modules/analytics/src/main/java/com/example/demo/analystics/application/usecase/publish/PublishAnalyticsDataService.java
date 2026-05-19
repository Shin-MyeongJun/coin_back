package com.example.demo.analystics.application.usecase.publish;

import com.example.demo.analystics.application.port.in.PublishAnalyticsDataUseCase;
import com.example.demo.analystics.application.port.out.SaveOutboxRecordPort;
import com.example.demo.analystics.domain.domain.AnalyticsOutboxRecord;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Function;

public abstract class PublishAnalyticsDataService<DOMAIN, MESSAGE> implements PublishAnalyticsDataUseCase<DOMAIN> {

    private final DomainToMessage<DOMAIN, MESSAGE> mapper;
    private final SaveOutboxRecordPort outboxSavePort;
    private final ObjectMapper objectMapper;
    private final String topic;
    private final String aggregateType;
    private final Function<MESSAGE, String> aggregateIdExtractor;

    protected PublishAnalyticsDataService(
            DomainToMessage<DOMAIN, MESSAGE> mapper,
            SaveOutboxRecordPort outboxSavePort,
            ObjectMapper objectMapper,
            String topic,
            String aggregateType,
            Function<MESSAGE, String> aggregateIdExtractor
    ) {
        this.mapper = mapper;
        this.outboxSavePort = outboxSavePort;
        this.objectMapper = objectMapper;
        this.topic = topic;
        this.aggregateType = aggregateType;
        this.aggregateIdExtractor = aggregateIdExtractor;
    }

    @Override
    public void publish(DOMAIN domain) {
        MESSAGE message = mapper.toMessage(domain);
        String payloadJson = serialize(message);
        AnalyticsOutboxRecord record = AnalyticsOutboxRecord.pending(
                aggregateType,
                aggregateIdExtractor.apply(message),
                topic,
                payloadJson,
                System.currentTimeMillis()
        );
        outboxSavePort.save(record);
    }

    private String serialize(MESSAGE message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize outbox payload aggregateType=" + aggregateType, e);
        }
    }
}
