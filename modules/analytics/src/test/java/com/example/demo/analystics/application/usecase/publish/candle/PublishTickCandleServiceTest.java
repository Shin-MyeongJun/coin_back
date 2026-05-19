package com.example.demo.analystics.application.usecase.publish.candle;

import com.example.demo.analystics.application.port.out.SaveOutboxRecordPort;
import com.example.demo.analystics.domain.domain.AnalyticsOutboxRecord;
import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.TimeWindow;
import com.example.demo.analystics.domain.domain.candle.close.TickCloseCandle;
import com.example.demo.contracts.message.candle.TickCandleMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PublishTickCandleServiceTest {

    @Mock
    DomainToMessage<TickCloseCandle, TickCandleMessage> mapper;

    @Mock
    SaveOutboxRecordPort outboxSavePort;

    ObjectMapper objectMapper;

    PublishTickCandleService sut;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        sut = new PublishTickCandleService(mapper, outboxSavePort, objectMapper);
    }

    @Test
    @DisplayName("publish — domain → message 매핑 후 JSON 직렬화하여 outbox 저장")
    void publish_savesOutboxRecord_withMessageJsonAndTopic() {
        // given
        TickCloseCandle domain = new TickCloseCandle(
                7L, Interval.M1,
                BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO, new BigDecimal("5"),
                new TimeWindow(0L, 60_000L, 1L, 59_000L));
        TickCandleMessage message = new TickCandleMessage(
                "7", "M1", "1", "10", "0", "5", "0", "60000", "1", "59000");
        given(mapper.toMessage(domain)).willReturn(message);

        // when
        sut.publish(domain);

        // then
        ArgumentCaptor<AnalyticsOutboxRecord> captor = ArgumentCaptor.forClass(AnalyticsOutboxRecord.class);
        then(outboxSavePort).should().save(captor.capture());
        AnalyticsOutboxRecord saved = captor.getValue();
        assertThat(saved.topic()).isEqualTo("analytics.tick-candle");
        assertThat(saved.aggregateType()).isEqualTo("TICK_CANDLE");
        assertThat(saved.aggregateId()).isEqualTo("7:M1");
        assertThat(saved.payloadJson()).contains("\"marketCodeId\":\"7\"");
        assertThat(saved.payloadJson()).contains("\"interval\":\"M1\"");
        assertThat(saved.publishedAt()).isNull();
        assertThat(saved.retryCount()).isZero();
        assertThat(saved.createdAt()).isPositive();
    }
}
