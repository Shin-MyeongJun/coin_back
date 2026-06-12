package com.example.demo.alert.application.usecase;

import com.example.demo.alert.application.port.out.ActiveRuleCachePort;
import com.example.demo.alert.application.port.out.DeleteAlertRulePort;
import com.example.demo.alert.application.port.out.LoadAlertRulePort;
import com.example.demo.alert.application.port.out.SaveAlertRulePort;
import com.example.demo.alert.domain.exception.AlertRuleNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AlertRuleCommandServiceTest {

    private static final String USER_ID = "user-1";
    private static final long MISSING_ID = 999L;

    @Mock LoadAlertRulePort loadAlertRulePort;
    @Mock SaveAlertRulePort saveAlertRulePort;
    @Mock DeleteAlertRulePort deleteAlertRulePort;
    @Mock ActiveRuleCachePort activeRuleCachePort;

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-12T00:00:00Z"), ZoneOffset.UTC);

    private AlertRuleCommandService sut;

    @BeforeEach
    void setUp() {
        sut = new AlertRuleCommandService(
                loadAlertRulePort, saveAlertRulePort, deleteAlertRulePort, activeRuleCachePort, clock);
    }

    @Test
    void update_throws_AlertRuleNotFound_when_rule_missing() {
        // given
        given(loadAlertRulePort.findByIdForUser(MISSING_ID, USER_ID)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> sut.update(USER_ID, MISSING_ID, null))
                .isInstanceOf(AlertRuleNotFoundException.class);
        verify(saveAlertRulePort, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void delete_throws_AlertRuleNotFound_when_rule_missing() {
        // given
        given(loadAlertRulePort.findByIdForUser(MISSING_ID, USER_ID)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> sut.delete(USER_ID, MISSING_ID))
                .isInstanceOf(AlertRuleNotFoundException.class);
        verify(deleteAlertRulePort, never()).deleteById(MISSING_ID);
    }

    @Test
    void toggle_throws_AlertRuleNotFound_when_rule_missing() {
        // given
        given(loadAlertRulePort.findByIdForUser(MISSING_ID, USER_ID)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> sut.toggle(USER_ID, MISSING_ID))
                .isInstanceOf(AlertRuleNotFoundException.class);
        verify(saveAlertRulePort, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
