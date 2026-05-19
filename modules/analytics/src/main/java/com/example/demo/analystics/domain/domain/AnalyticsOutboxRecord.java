package com.example.demo.analystics.domain.domain;

public record AnalyticsOutboxRecord(
        Long id,
        String aggregateType,
        String aggregateId,
        String topic,
        String payloadJson,
        Long createdAt,
        Long publishedAt,
        int retryCount
) {

    public static AnalyticsOutboxRecord pending(String aggregateType,
                                                String aggregateId,
                                                String topic,
                                                String payloadJson,
                                                long createdAt) {
        return new AnalyticsOutboxRecord(null, aggregateType, aggregateId, topic, payloadJson, createdAt, null, 0);
    }
}
