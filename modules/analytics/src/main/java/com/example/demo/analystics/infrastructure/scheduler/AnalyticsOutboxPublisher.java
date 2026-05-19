package com.example.demo.analystics.infrastructure.scheduler;

import com.example.demo.analystics.application.port.out.LoadPendingOutboxPort;
import com.example.demo.analystics.application.port.out.MarkOutboxPublishedPort;
import com.example.demo.analystics.domain.domain.AnalyticsOutboxRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AnalyticsOutboxPublisher {

    private final LoadPendingOutboxPort loadPort;
    private final MarkOutboxPublishedPort markPort;
    private final KafkaTemplate<String, String> outboxKafkaTemplate;

    private final int batchSize;
    private final int maxRetry;

    public AnalyticsOutboxPublisher(
            LoadPendingOutboxPort loadPort,
            MarkOutboxPublishedPort markPort,
            @Qualifier("outboxKafkaTemplate") KafkaTemplate<String, String> outboxKafkaTemplate,
            @Value("${analytics.outbox.batch-size:200}") int batchSize,
            @Value("${analytics.outbox.max-retry:10}") int maxRetry
    ) {
        this.loadPort = loadPort;
        this.markPort = markPort;
        this.outboxKafkaTemplate = outboxKafkaTemplate;
        this.batchSize = batchSize;
        this.maxRetry = maxRetry;
    }

    @Scheduled(fixedDelayString = "${analytics.outbox.poll-interval-ms:500}")
    public void publishPending() {
        List<AnalyticsOutboxRecord> pending = loadPort.loadPending(batchSize, maxRetry);
        if (pending.isEmpty()) {
            return;
        }
        for (AnalyticsOutboxRecord record : pending) {
            try {
                outboxKafkaTemplate
                        .send(record.topic(), record.aggregateId(), record.payloadJson())
                        .get();
                markPort.markPublished(record.id(), System.currentTimeMillis());
            } catch (Exception ex) {
                markPort.incrementRetry(record.id());
                int nextRetry = record.retryCount() + 1;
                if (nextRetry >= maxRetry) {
                    log.error("[OutboxPublisher] dead-letter outbox id={} topic={} aggregateId={} retry={}",
                            record.id(), record.topic(), record.aggregateId(), nextRetry, ex);
                } else {
                    log.warn("[OutboxPublisher] publish failed outbox id={} topic={} retry={}",
                            record.id(), record.topic(), nextRetry, ex);
                }
            }
        }
    }
}
