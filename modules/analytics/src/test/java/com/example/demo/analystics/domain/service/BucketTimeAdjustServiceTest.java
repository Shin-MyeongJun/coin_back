package com.example.demo.analystics.domain.service;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.TimeWindow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BucketTimeAdjustServiceTest {

    private final BucketTimeAdjustService sut = new BucketTimeAdjustService();

    @Test
    @DisplayName("M1 — close가 정각(00:01:00.000)이면 bucketClose=60000, bucketOpen=0")
    void adjust_m1_atExactBoundary() {
        // given  close = 60_000ms (정확히 1분 정각, effectiveClose = 59_999)
        long open  = 1_000L;
        long close = 60_000L;

        // when
        TimeWindow tw = sut.adjust(open, close, Interval.M1);

        // then
        assertThat(tw.observeOpenTs()).isEqualTo(open);
        assertThat(tw.observeCloseTs()).isEqualTo(close);
        assertThat(tw.bucketCloseTs()).isEqualTo(60_000L);
        assertThat(tw.bucketOpenTs()).isEqualTo(0L);
    }

    @Test
    @DisplayName("M1 — close가 00:01:30이면 bucketClose=120000, bucketOpen=60000")
    void adjust_m1_midInterval() {
        // given  90_000ms (1분 30초)
        long open  = 65_000L;
        long close = 90_000L;

        // when
        TimeWindow tw = sut.adjust(open, close, Interval.M1);

        // then
        assertThat(tw.bucketCloseTs()).isEqualTo(120_000L);
        assertThat(tw.bucketOpenTs()).isEqualTo(60_000L);
    }

    @Test
    @DisplayName("M5 — close가 5분 구간 안에 있으면 bucket이 5분 단위로 정렬")
    void adjust_m5_bucketAlignedToFiveMinutes() {
        // given  7분 정도에 close
        long open  = 300_500L;
        long close = 420_000L; // 7min, effectiveClose=419999 → bucket = ceil5min → 600000

        // when
        TimeWindow tw = sut.adjust(open, close, Interval.M5);

        long millis5 = 5 * 60_000L;
        // then
        assertThat(tw.bucketCloseTs() % millis5).isZero();
        assertThat(tw.bucketOpenTs()).isEqualTo(tw.bucketCloseTs() - millis5);
    }

    @Test
    @DisplayName("observeTimestamps는 입력값 그대로 보존")
    void adjust_observeTimestampsPreserved() {
        // given
        long open  = 123_456L;
        long close = 234_567L;

        // when
        TimeWindow tw = sut.adjust(open, close, Interval.M1);

        // then
        assertThat(tw.observeOpenTs()).isEqualTo(open);
        assertThat(tw.observeCloseTs()).isEqualTo(close);
    }
}
