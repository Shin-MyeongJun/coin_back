package com.example.demo.analystics.infrastructure.persistence.mapper;

import com.example.demo.analystics.domain.domain.AnalyticsOutboxRecord;
import com.example.demo.analystics.infrastructure.persistence.entity.AnalyticsOutboxEntity;
import com.example.demo.infra_shard.persistence.EntityMapping;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsOutboxEntityMapper implements EntityMapping<AnalyticsOutboxRecord, AnalyticsOutboxEntity> {

    @Override
    public AnalyticsOutboxEntity toEntity(AnalyticsOutboxRecord record) {
        return AnalyticsOutboxEntity.builder()
                .id(record.id())
                .aggregateType(record.aggregateType())
                .aggregateId(record.aggregateId())
                .topic(record.topic())
                .payloadJson(record.payloadJson())
                .createdAt(record.createdAt())
                .publishedAt(record.publishedAt())
                .retryCount(record.retryCount())
                .build();
    }

    @Override
    public AnalyticsOutboxRecord toDomain(AnalyticsOutboxEntity entity) {
        return new AnalyticsOutboxRecord(
                entity.getId(),
                entity.getAggregateType(),
                entity.getAggregateId(),
                entity.getTopic(),
                entity.getPayloadJson(),
                entity.getCreatedAt(),
                entity.getPublishedAt(),
                entity.getRetryCount()
        );
    }
}
