package com.example.demo.analystics.domain.store.candle;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.close.TickCloseCandle;
import com.example.demo.analystics.domain.domain.candle.open.TickCandle;
import com.example.demo.analystics.domain.domain.candle.value.OhlcData;
import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.analystics.domain.service.ClosingData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TickCandleStoreTest {

    @Mock
    ClosingData<TickCandle, TickCloseCandle> closer;

    TickCandleStore sut;
    TickKey key1 = new TickKey(1L);
    TickKey key2 = new TickKey(2L);

    @BeforeEach
    void setUp() {
        sut = new TickCandleStore(closer);
    }

    @Test
    @DisplayName("update — 새 key면 open=val, getCandles로 조회 가능")
    void update_newKey_candleCreated() {
        // given / when
        sut.update(key1, new BigDecimal("100"));

        // then — 모든 interval에 candle 생성
        for (Interval interval : Interval.analyticsSupported()) {
            assertThat(sut.getCandles(interval)).hasSize(1);
        }
    }

    @Test
    @DisplayName("update — 같은 key로 두 번 호출하면 high/low/close 갱신")
    void update_sameKey_updatesOhlc() {
        // given
        sut.update(key1, new BigDecimal("100"));

        // when
        sut.update(key1, new BigDecimal("200"));

        // then
        List<TickCandle> candles = sut.getCandles(Interval.M1);
        assertThat(candles).hasSize(1);
        TickCandle candle = candles.getFirst();
        assertThat(candle.getOpen()).isEqualByComparingTo("100");
        assertThat(candle.getHigh()).isEqualByComparingTo("200");
        assertThat(candle.getClose()).isEqualByComparingTo("200");
    }

    @Test
    @DisplayName("drain — closer.toClose 결과를 반환하고 buffer를 비움")
    void drain_returnsClosedCandlesAndClearsBuffer() {
        // given
        sut.update(key1, new BigDecimal("100"));
        TickCloseCandle fakeClosed = new TickCloseCandle(1L, Interval.M1,
                new BigDecimal("100"), new BigDecimal("100"),
                new BigDecimal("100"), new BigDecimal("100"), null);
        given(closer.toClose(any(TickCandle.class), eq(Interval.M1))).willReturn(fakeClosed);

        // when
        List<TickCloseCandle> result = sut.drain(Interval.M1);

        // then
        assertThat(result).hasSize(1).contains(fakeClosed);
        assertThat(sut.getCandles(Interval.M1)).isEmpty();
    }

    @Test
    @DisplayName("drain — 빈 store이면 빈 리스트 반환")
    void drain_emptyStore_returnsEmptyList() {
        // when
        List<TickCloseCandle> result = sut.drain(Interval.M1);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("assign — snapshot으로 buffer를 교체")
    void assign_replacesExistingBuffer() {
        // given
        sut.update(key1, new BigDecimal("100")); // 기존 데이터

        TickCandle restored = new TickCandle(key2,
                new OhlcData<>(new BigDecimal("50"), new BigDecimal("80"),
                        new BigDecimal("40"), new BigDecimal("60")), 1_000L);
        Map<Interval, List<TickCandle>> snapshot = Map.of(Interval.M1, List.of(restored));

        // when
        sut.assign(snapshot);

        // then
        List<TickCandle> candles = sut.getCandles(Interval.M1);
        assertThat(candles).hasSize(1);
        assertThat(candles.getFirst().getKey()).isEqualTo(key2);
    }

    @Test
    @DisplayName("getCandles — 미지원 interval이면 빈 리스트")
    void getCandles_unsupportedInterval_returnsEmpty() {
        // given
        sut.update(key1, new BigDecimal("100"));

        // when
        List<TickCandle> result = sut.getCandles(Interval.D1);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @Tag("concurrency")
    @DisplayName("동시 update — ConcurrentHashMap으로 데이터 손실 없음")
    void update_concurrent_noDataLoss() throws InterruptedException {
        // given
        int threadCount = 20;
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        try (ExecutorService pool = Executors.newFixedThreadPool(threadCount)) {
            for (int i = 0; i < threadCount; i++) {
                long id = i;
                pool.submit(() -> {
                    sut.update(new TickKey(id), new BigDecimal(String.valueOf(id * 100)));
                    latch.countDown();
                });
            }
            latch.await();
        }

        // then — threadCount 개의 서로 다른 key → M1 buffer에 threadCount candle 존재
        assertThat(sut.getCandles(Interval.M1)).hasSize(threadCount);
    }
}
