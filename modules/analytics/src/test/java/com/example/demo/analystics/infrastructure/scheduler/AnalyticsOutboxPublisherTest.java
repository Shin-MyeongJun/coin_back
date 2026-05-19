package com.example.demo.analystics.infrastructure.scheduler;

import com.example.demo.analystics.application.port.out.LoadPendingOutboxPort;
import com.example.demo.analystics.application.port.out.MarkOutboxPublishedPort;
import com.example.demo.analystics.domain.domain.AnalyticsOutboxRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AnalyticsOutboxPublisherTest {

    @Mock
    LoadPendingOutboxPort loadPort;

    @Mock
    MarkOutboxPublishedPort markPort;

    @Mock
    KafkaTemplate<String, String> outboxKafkaTemplate;

    AnalyticsOutboxPublisher sut;

    @BeforeEach
    void setUp() {
        sut = new AnalyticsOutboxPublisher(loadPort, markPort, outboxKafkaTemplate, 100, 3);
    }

    private static AnalyticsOutboxRecord pending(long id, int retryCount) {
        return new AnalyticsOutboxRecord(
                id, "TICK_CANDLE", "agg-" + id, "analytics.tick-candle",
                "{\"v\":" + id + "}", 0L, null, retryCount);
    }

    @Test
    @DisplayName("publishPending — pending 없음 → no-op")
    void publishPending_noPending_noOp() {
        // given
        given(loadPort.loadPending(100, 3)).willReturn(List.of());

        // when
        sut.publishPending();

        // then
        then(outboxKafkaTemplate).shouldHaveNoInteractions();
        then(markPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("publishPending — 성공 시 KafkaTemplate.send 후 markPublished 호출")
    void publishPending_success_marksPublished() throws Exception {
        // given
        AnalyticsOutboxRecord r = pending(1L, 0);
        given(loadPort.loadPending(100, 3)).willReturn(List.of(r));

        CompletableFuture<SendResult<String, String>> done = CompletableFuture.completedFuture(
                new SendResult<>(null, (RecordMetadata) null));
        given(outboxKafkaTemplate.send("analytics.tick-candle", "agg-1", "{\"v\":1}"))
                .willReturn(done);

        // when
        sut.publishPending();

        // then
        then(outboxKafkaTemplate).should().send("analytics.tick-candle", "agg-1", "{\"v\":1}");
        then(markPort).should().markPublished(eq(1L), anyLong());
        then(markPort).should(never()).incrementRetry(anyLong());
    }

    @Test
    @DisplayName("publishPending — Kafka send 실패 시 retryCount 증가")
    void publishPending_failure_incrementsRetry() {
        // given
        AnalyticsOutboxRecord r = pending(2L, 0);
        given(loadPort.loadPending(100, 3)).willReturn(List.of(r));

        CompletableFuture<SendResult<String, String>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("broker down"));
        given(outboxKafkaTemplate.send("analytics.tick-candle", "agg-2", "{\"v\":2}"))
                .willReturn(failed);

        // when
        sut.publishPending();

        // then
        then(markPort).should().incrementRetry(2L);
        then(markPort).should(never()).markPublished(anyLong(), anyLong());
    }

    @Test
    @DisplayName("publishPending — max-retry 초과 시에도 incrementRetry는 호출 (dead-letter 로그는 부수효과)")
    void publishPending_maxRetryExceeded_incrementsRetryAndLogs() {
        // given: retryCount 가 max-retry(3) 직전인 2 → 실패 후 3 에 도달 (dead-letter)
        AnalyticsOutboxRecord r = pending(3L, 2);
        given(loadPort.loadPending(100, 3)).willReturn(List.of(r));

        CompletableFuture<SendResult<String, String>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("broker down"));
        given(outboxKafkaTemplate.send("analytics.tick-candle", "agg-3", "{\"v\":3}"))
                .willReturn(failed);

        // when
        sut.publishPending();

        // then
        then(markPort).should().incrementRetry(3L);
        then(markPort).should(never()).markPublished(anyLong(), anyLong());
    }
}
