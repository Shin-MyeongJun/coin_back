package com.example.demo.api.config.security.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitDecisionTest {

    @Test
    @DisplayName("retryAfterSeconds 는 ms 를 ceil 한다")
    void retryAfterSecondsCeil() {
        assertThat(RateLimitDecision.deny(0L).retryAfterSeconds()).isEqualTo(0L);
        assertThat(RateLimitDecision.deny(1L).retryAfterSeconds()).isEqualTo(1L);
        assertThat(RateLimitDecision.deny(999L).retryAfterSeconds()).isEqualTo(1L);
        assertThat(RateLimitDecision.deny(1000L).retryAfterSeconds()).isEqualTo(1L);
        assertThat(RateLimitDecision.deny(1001L).retryAfterSeconds()).isEqualTo(2L);
        assertThat(RateLimitDecision.deny(45_000L).retryAfterSeconds()).isEqualTo(45L);
    }

    @Test
    @DisplayName("음수 retryAfterMs 는 0 으로 정규화")
    void negativeNormalized() {
        assertThat(RateLimitDecision.deny(-1L).retryAfterMs()).isZero();
    }

    @Test
    @DisplayName("allow() 는 retryAfterMs=0, allowed=true")
    void allow() {
        RateLimitDecision d = RateLimitDecision.allow();
        assertThat(d.allowed()).isTrue();
        assertThat(d.retryAfterMs()).isZero();
    }
}
