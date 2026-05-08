package com.example.demo.analystics.domain.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TimeWindowTest {

    @Test
    @DisplayName("허용 범위(5000ms) 이내이면 isAcceptableDrift = true")
    void isAcceptableDrift_withinTolerance_returnsTrue() {
        // given
        long bucketOpen  = 60_000L;
        long bucketClose = 120_000L;
        TimeWindow tw = new TimeWindow(bucketOpen, bucketClose,
                bucketOpen + 1000, bucketClose + 1000);

        // when / then
        assertThat(tw.isAcceptableDrift()).isTrue();
        assertThat(tw.isOpenDrifted()).isFalse();
        assertThat(tw.isCloseDrifted()).isFalse();
    }

    @Test
    @DisplayName("open 드리프트가 5000ms 초과이면 isOpenDrifted = true")
    void isOpenDrifted_exceeds5s_returnsTrue() {
        // given
        TimeWindow tw = new TimeWindow(60_000L, 120_000L,
                60_000L + 6_000L, 120_000L + 1_000L);

        // when / then
        assertThat(tw.isOpenDrifted()).isTrue();
        assertThat(tw.isAcceptableDrift()).isFalse();
    }

    @Test
    @DisplayName("close 드리프트가 5000ms 초과이면 isCloseDrifted = true")
    void isCloseDrifted_exceeds5s_returnsTrue() {
        // given
        TimeWindow tw = new TimeWindow(60_000L, 120_000L,
                60_000L + 1_000L, 120_000L + 6_000L);

        // when / then
        assertThat(tw.isCloseDrifted()).isTrue();
        assertThat(tw.isAcceptableDrift()).isFalse();
    }

    @Test
    @DisplayName("null 타임스탬프는 드리프트 허용 안 함")
    void nullTimestamp_notAcceptable() {
        // given
        TimeWindow tw = new TimeWindow(null, 120_000L, null, 120_000L);

        // when / then
        assertThat(tw.isAcceptableDrift()).isFalse();
    }

    @Test
    @DisplayName("경계값 정확히 5000ms 차이이면 isAcceptableDrift = true")
    void isAcceptableDrift_exactBoundary_returnsTrue() {
        // given
        TimeWindow tw = new TimeWindow(60_000L, 120_000L,
                60_000L + 5_000L, 120_000L + 5_000L);

        // when / then
        assertThat(tw.isAcceptableDrift()).isTrue();
    }
}
