package com.example.demo.analystics.application.usecase.flush_and_on;

import com.example.demo.analystics.application.port.in.PublishAnalyticsDataUseCase;
import com.example.demo.analystics.application.port.out.WriteAnalyticsValuePort;
import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.close.PremiumCloseCandle;
import com.example.demo.analystics.domain.domain.indicator.TradeIndicatorType;
import com.example.demo.analystics.domain.domain.indicator.close.PremiumCloseIndicator;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.partition_registry.PremiumPartitionRegistry;
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
class PremiumAnalyticsServiceTest {

    @Mock
    PremiumPartitionRegistry registry;

    @Mock
    WriteAnalyticsValuePort<PremiumCloseCandle> candleWriter;

    @Mock
    WriteAnalyticsValuePort<PremiumCloseIndicator> indicatorWriter;

    @Mock
    PublishAnalyticsDataUseCase<PremiumCloseCandle> candlePublisher;

    @Mock
    PublishAnalyticsDataUseCase<PremiumCloseIndicator> indicatorPublisher;

    PremiumAnalyticsService sut;

    @BeforeEach
    void setUp() {
        sut = new PremiumAnalyticsService(registry, candleWriter, indicatorWriter, candlePublisher, indicatorPublisher);
    }

    @Test
    @DisplayName("onData ??registry.update(partitionId, key, price) ?꾩엫")
    void onData_delegatesToRegistry() {
        // given
        PremiumKey key = new PremiumKey("BTC", 1L, 2L);

        // when
        sut.onData(0, key, new BigDecimal("1.25"));

        // then
        then(registry).should().update(0, key, new BigDecimal("1.25"));
    }

    @Test
    @DisplayName("flushCandles ??non-empty ??DB write ??Kafka publish ?쒖꽌濡?泥섎━")
    void flushCandles_nonEmpty_writesAndPublishes() {
        // given
        PremiumCloseCandle fake = new PremiumCloseCandle(
                "BTC", 1L, 2L, Interval.M1,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, null);
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

    @Test
    @DisplayName("flushIndicators ??non-empty ??DB write ??Kafka publish ?쒖꽌濡?泥섎━")
    void flushIndicators_nonEmpty_writesAndPublishes() {
        // given
        PremiumCloseIndicator fake = new PremiumCloseIndicator(
                "BTC", 1L, 2L, Interval.M1, TradeIndicatorType.EMA, 14, BigDecimal.TEN, null);
        given(registry.flushIndicators(Interval.M1)).willReturn(List.of(fake));

        // when
        sut.flushIndicators(Interval.M1);

        // then
        InOrder inOrder = inOrder(indicatorWriter, indicatorPublisher);
        inOrder.verify(indicatorWriter).write(List.of(fake));
        inOrder.verify(indicatorPublisher).publish(fake);
    }

    @Test
    @DisplayName("flushIndicators ??empty ??write/publish 誘명샇異?")
    void flushIndicators_empty_skipWriteAndPublish() {
        // given
        given(registry.flushIndicators(Interval.M1)).willReturn(List.of());

        // when
        sut.flushIndicators(Interval.M1);

        // then
        then(indicatorWriter).should(never()).write(java.util.Collections.emptyList());
        then(indicatorPublisher).shouldHaveNoInteractions();
    }
}
