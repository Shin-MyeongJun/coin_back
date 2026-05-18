package com.example.demo.alert.application.port.in;

import com.example.demo.alert.application.usecase.AlertRuleCommand;
import com.example.demo.alert.domain.domain.AlertRule;

public interface UpdateAlertRuleUseCase {
    AlertRule update(String userId, long id, AlertRuleCommand command);
}
