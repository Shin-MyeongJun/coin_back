package com.example.demo.alert.application.port.in;

import com.example.demo.alert.application.usecase.AlertRuleCommand;
import com.example.demo.alert.domain.domain.AlertRule;

public interface RegisterAlertRuleUseCase {
    AlertRule register(String userId, AlertRuleCommand command);
}
