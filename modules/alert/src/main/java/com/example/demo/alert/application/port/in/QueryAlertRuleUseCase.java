package com.example.demo.alert.application.port.in;

import com.example.demo.alert.application.usecase.AlertRulePage;
import com.example.demo.alert.domain.domain.AlertRule;

public interface QueryAlertRuleUseCase {
    AlertRule get(String userId, long id);

    AlertRulePage findByUser(String userId, int page, int size);
}
