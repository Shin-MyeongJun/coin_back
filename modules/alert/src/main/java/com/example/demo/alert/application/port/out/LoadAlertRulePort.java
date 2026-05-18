package com.example.demo.alert.application.port.out;

import com.example.demo.alert.application.usecase.AlertRulePage;
import com.example.demo.alert.domain.domain.AlertRule;
import com.example.demo.alert.domain.domain.TargetType;

import java.util.List;
import java.util.Optional;

public interface LoadAlertRulePort {
    Optional<AlertRule> findByIdForUser(long id, String userId);

    AlertRulePage findByUser(String userId, int page, int size);

    List<AlertRule> findAllActive();

    List<AlertRule> findActiveByTargetAndAsset(TargetType targetType, String assetSymbol);
}
