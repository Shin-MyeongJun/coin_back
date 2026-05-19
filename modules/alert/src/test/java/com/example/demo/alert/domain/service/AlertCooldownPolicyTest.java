package com.example.demo.alert.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlertCooldownPolicyTest {

    private final AlertCooldownPolicy sut = new AlertCooldownPolicy();

    @Test
    @DisplayName("lastFiredAt이 null이면 항상 허용")
    void isAllowed_nullLastFiredAt_allowed() {
        assertThat(sut.isAllowed(null, 30, 1_000_000L)).isTrue();
    }

    @Test
    @DisplayName("cooldown이 0 이하이면 항상 허용")
    void isAllowed_nonPositiveCooldown_allowed() {
        assertThat(sut.isAllowed(1_000_000L, 0, 1_000_000L)).isTrue();
        assertThat(sut.isAllowed(1_000_000L, -10, 1_000_000L)).isTrue();
    }

    @Test
    @DisplayName("경과 시간 >= cooldown이면 허용")
    void isAllowed_cooldownElapsed_allowed() {
        long last = 1_000_000L;
        int cooldownSec = 30;
        long now = last + 30_000L;

        assertThat(sut.isAllowed(last, cooldownSec, now)).isTrue();
    }

    @Test
    @DisplayName("경과 시간 < cooldown이면 거부")
    void isAllowed_cooldownNotElapsed_denied() {
        long last = 1_000_000L;
        int cooldownSec = 30;
        long now = last + 29_999L;

        assertThat(sut.isAllowed(last, cooldownSec, now)).isFalse();
    }

    @Test
    @DisplayName("동일 시각이면 거부 (경과 0)")
    void isAllowed_sameInstant_denied() {
        long last = 1_000_000L;

        assertThat(sut.isAllowed(last, 30, last)).isFalse();
    }
}
