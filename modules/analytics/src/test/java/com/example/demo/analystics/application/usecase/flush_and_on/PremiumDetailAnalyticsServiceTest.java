package com.example.demo.analystics.application.usecase.flush_and_on;

import com.example.demo.analystics.application.port.in.PublishAnalyticsDataUseCase;
import com.example.demo.analystics.application.port.out.WriteAnalyticsValuePort;
import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.close.PremiumDetailCloseCandle;
import com.example.demo.analystics.domain.domain.candle.value.PremiumDetailValue;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.partition_registry.PremiumDetailPartitionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PremiumDetailAnalyticsServiceTest {

    @Mock
    PremiumDetailPartitionRegistry registry;

    @Mock
    WriteAnalyticsValuePort<PremiumDetailCloseCandle> candleWriter;

    @Mock
    PublishAnalyticsDataUseCase<PremiumDetailCloseCandle> candlePublisher;

    PremiumDetailAnalyticsService sut;

    @BeforeEach
    void setUp() {
        sut = new PremiumDetailAnalyticsService(registry, candleWriter, candlePublisher);
    }

    @Test
    @DisplayName("onData ??registry.update(partitionId, key, value) ?꾩엫")
    void onData_delegatesToRegistry() {
        // given
        PremiumKey key = new PremiumKey("BTC", 1L, 2L);
        PremiumDetailValue value = detail();

        // when
        sut.onData(0, key, value);

        // then
        then(registry).should().update(0, key, value);
    }

    @Test
    @DisplayName("flushCandles ??non-empty ??DB write ??Kafka publish ?쒖꽌濡?泥섎━")
    void flushCandles_nonEmpty_writesAndPublishes() {
        // given
        PremiumDetailCloseCandle fake = new PremiumDetailCloseCandle(
                "BTC", 1L, 2L, Interval.M1, detail(), detail(), detail(), detail(), null);
        given(registry.flushCandles(Interval.M1)).willReturn(List.of(fake));

        // when
        sut.flushCandles(Interval.M1);

        // then
        InOrder inOrder = inOrder(candleWriter, candlePublisher);
        inOrder.verify(candleWriter).write(List.of(fake));
        inOrder.verify(candlePublisher).publish(fake);
    }

    @Test
    @DisplayName("flushCandles ??empty ??write/publish 誘명샇異?")
    void flushCandles_empty_skipWriteAndPublish() {
        // given
        given(registry.flushCandles(Interval.M1)).willReturn(List.of());

        // when
        sut.flushCandles(Interval.M1);

        // then
        then(candleWriter).should(never()).write(java.util.Collections.emptyList());
        then(candlePublisher).shouldHaveNoInteractions();
    }

    private static PremiumDetailValue detail() {
        return new PremiumDetailValue(
                BigDecimal.ONE,
                BigDecimal.TEN,
                BigDecimal.ONE,
                BigDecimal.TEN
        );
    }
}
