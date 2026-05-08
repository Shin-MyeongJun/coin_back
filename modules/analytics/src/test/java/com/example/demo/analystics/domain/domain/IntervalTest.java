package com.example.demo.analystics.domain.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntervalTest {

    @Test
    @DisplayName("analyticsSupported — M1~M240 7개 반환")
    void analyticsSupported_returns7Intervals() {
        // when
        List<Interval> supported = Interval.analyticsSupported();

        // then
        assertThat(supported).hasSize(7)
                .containsExactly(Interval.M1, Interval.M3, Interval.M5,
                        Interval.M15, Interval.M30, Interval.M60, Interval.M240);
    }

    @Test
    @DisplayName("fromMinutes — 정상 변환")
    void fromMinutes_validMinutes_returnsInterval() {
        assertThat(Interval.fromMinutes(1)).isEqualTo(Interval.M1);
        assertThat(Interval.fromMinutes(60)).isEqualTo(Interval.M60);
        assertThat(Interval.fromMinutes(240)).isEqualTo(Interval.M240);
    }

    @Test
    @DisplayName("fromMinutes — 미지원 분 → IllegalArgumentException")
    void fromMinutes_unsupported_throws() {
        assertThatThrownBy(() -> Interval.fromMinutes(999))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("fromPeriod — 정상 변환 (대소문자 무시)")
    void fromPeriod_validPeriod_returnsInterval() {
        assertThat(Interval.fromPeriod("1m")).isEqualTo(Interval.M1);
        assertThat(Interval.fromPeriod("1H")).isEqualTo(Interval.M60);
        assertThat(Interval.fromPeriod("4h")).isEqualTo(Interval.M240);
    }

    @Test
    @DisplayName("fromPeriod — 미지원 문자열 → IllegalArgumentException")
    void fromPeriod_unsupported_throws() {
        assertThatThrownBy(() -> Interval.fromPeriod("99x"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("getSecond — minutes * 60")
    void getSecond_returnsMinutesTimessixty() {
        assertThat(Interval.M1.getSecond()).isEqualTo(60);
        assertThat(Interval.M60.getSecond()).isEqualTo(3600);
    }
}
