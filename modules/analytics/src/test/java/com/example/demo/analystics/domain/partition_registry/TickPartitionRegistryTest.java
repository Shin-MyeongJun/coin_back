package com.example.demo.analystics.domain.partition_registry;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.close.TickCloseCandle;
import com.example.demo.analystics.domain.domain.candle.open.TickCandle;
import com.example.demo.analystics.domain.domain.indicator.close.TickCloseIndicator;
import com.example.demo.analystics.domain.domain.indicator.open.TickIndicator;
import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.analystics.domain.factory.indicator.value.TickIndicatorFactory;
import com.example.demo.analystics.domain.service.ClosingData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TickPartitionRegistryTest {

    @Mock
    ClosingData<TickCandle, TickCloseCandle> candleCloser;

    @Mock
    ClosingData<TickIndicator, TickCloseIndicator> indicatorCloser;

    TickIndicatorFactory indicatorFactory = new TickIndicatorFactory();

    TickPartitionRegistry sut;

    @BeforeEach
    void setUp() {
        sut = new TickPartitionRegistry(candleCloser, indicatorFactory, indicatorCloser);
    }

    @Test
    @DisplayName("assignPartition — 파티션 store 생성, getActivePartitionIds에 포함")
    void assignPartition_addsToActivePartitions() {
        // given / when
        sut.assignPartition(0);
        sut.assignPartition(1);

        // then
        assertThat(sut.getActivePartitionIds()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("revokePartitions — 지정 파티션 store 제거")
    void revokePartitions_removesStores() {
        // given
        sut.assignPartition(0);
        sut.assignPartition(1);

        // when
        sut.revokePartitions(List.of(0));

        // then
        assertThat(sut.getActivePartitionIds()).containsExactly(1);
    }

    @Test
    @DisplayName("update — store 없는 파티션은 skip (예외 없음)")
    void update_missingPartition_silentlySkips() {
        // when / then — no exception
        sut.update(99, new TickKey(1L), new BigDecimal("100"));
    }

    @Test
    @DisplayName("update — store가 있는 파티션은 candle 및 indicator 업데이트")
    void update_existingPartition_updatesStores() {
        // given
        sut.assignPartition(0);

        // when
        sut.update(0, new TickKey(1L), new BigDecimal("100"));

        // then
        assertThat(sut.getCandles(0, Interval.M1)).hasSize(1);
        assertThat(sut.getIndicators(0, Interval.M1)).hasSize(8);
    }

    @Test
    @DisplayName("flushCandles — 모든 파티션의 drain 결과를 합산")
    void flushCandles_aggregatesAcrossPartitions() {
        // given
        sut.assignPartition(0);
        sut.assignPartition(1);

        sut.update(0, new TickKey(1L), new BigDecimal("100"));
        sut.update(0, new TickKey(1L), new BigDecimal("110")); // prevSub for closer
        sut.update(1, new TickKey(2L), new BigDecimal("200"));
        sut.update(1, new TickKey(2L), new BigDecimal("210"));

        TickCloseCandle fake = new TickCloseCandle(1L, Interval.M1,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, null);
        given(candleCloser.toClose(any(TickCandle.class), eq(Interval.M1))).willReturn(fake);

        // when
        List<TickCloseCandle> result = sut.flushCandles(Interval.M1);

        // then — partition 0: 1 candle, partition 1: 1 candle → total 2
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getCandles — store 없는 파티션은 빈 리스트")
    void getCandles_missingPartition_returnsEmpty() {
        // when
        List<TickCandle> result = sut.getCandles(99, Interval.M1);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("restoreCandles — store가 없으면 auto-assign 후 snapshot 주입")
    void restoreCandles_autoAssignsIfMissing() {
        // given  no partition 5
        TickCandle restored = new TickCandle(new TickKey(10L), new BigDecimal("50"));
        java.util.Map<Interval, List<TickCandle>> snapshot =
                java.util.Map.of(Interval.M1, List.of(restored));

        // when
        sut.restoreCandles(5, snapshot);

        // then
        assertThat(sut.getActivePartitionIds()).contains(5);
        assertThat(sut.getCandles(5, Interval.M1)).hasSize(1);
    }
}
