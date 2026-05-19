package com.example.demo.alert.application.port.out;

import com.example.demo.alert.domain.domain.AlertRule;
import com.example.demo.alert.domain.domain.TargetType;

import java.util.List;

public interface ActiveAlertRuleStorePort {
    List<AlertRule> findActive(TargetType targetType, String assetSymbol);

    void refresh();
}
