package com.example.demo.analystics.application.usecase.flush_and_on;

import com.example.demo.analystics.application.port.out.WriteAnalyticsValuePort;
import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.close.TickCloseCandle;
import com.example.demo.analystics.domain.domain.indicator.close.TickCloseIndicator;
import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.analystics.domain.partition_registry.TickPartitionRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TickAnalyticsServiceTest {

    @Mock
    TickPartitionRegistry registry;

    @Mock
    WriteAnalyticsValuePort<TickCloseCandle> candleWriter;

    @Mock
    WriteAnalyticsValuePort<TickCloseIndicator> indicatorWriter;

    @InjectMocks
    TickAnalyticsService sut;

    @Test
    @DisplayName("onData — registry.update(partitionId, key, price) 위임")
    void onData_delegatesToRegistry() {
        // given
        TickKey key = new TickKey(1L);

        // when
        sut.onData(0, key, new BigDecimal("100"));

        // then
        then(registry).should().update(0, key, new BigDecimal("100"));
    }

    @Test
    @DisplayName("flushCandles — non-empty → candleWriter.write 호출")
    void flushCandles_nonEmpty_writesCandles() {
        // given
        TickCloseCandle fake = new TickCloseCandle(1L, Interval.M1,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, null);
        given(registry.flushCandles(Interval.M1)).willReturn(List.of(fake));

        // when
        sut.flushCandles(Interval.M1);

        // then
        then(candleWriter).should().write(List.of(fake));
    }

    @Test
    @DisplayName("flushCandles — empty → candleWriter.write 미호출")
    void flushCandles_empty_skipWrite() {
        // given
        given(registry.flushCandles(Interval.M1)).willReturn(List.of());

        // when
        sut.flushCandles(Interval.M1);

        // then
        then(candleWriter).should(never()).write(java.util.Collections.emptyList());
    }

    @Test
    @DisplayName("flushIndicators — non-empty → indicatorWriter.write 호출")
    void flushIndicators_nonEmpty_writesIndicators() {
        // given
        TickCloseIndicator fake = new TickCloseIndicator(
                1L, Interval.M1, null, 0, BigDecimal.ZERO, null);
        given(registry.flushIndicators(Interval.M1)).willReturn(List.of(fake));

        // when
        sut.flushIndicators(Interval.M1);

        // then
        then(indicatorWriter).should().write(List.of(fake));
    }

    @Test
    @DisplayName("flushIndicators — empty → indicatorWriter.write 미호출")
    void flushIndicators_empty_skipWrite() {
        // given
        given(registry.flushIndicators(Interval.M1)).willReturn(List.of());

        // when
        sut.flushIndicators(Interval.M1);

        // then
        then(indicatorWriter).should(never()).write(java.util.Collections.emptyList());
    }
}
