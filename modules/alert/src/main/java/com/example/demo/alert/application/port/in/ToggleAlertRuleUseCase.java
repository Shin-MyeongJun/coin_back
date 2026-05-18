package com.example.demo.alert.application.port.in;

import com.example.demo.alert.domain.domain.AlertRule;

public interface ToggleAlertRuleUseCase {
    AlertRule toggle(String userId, long id);
}
