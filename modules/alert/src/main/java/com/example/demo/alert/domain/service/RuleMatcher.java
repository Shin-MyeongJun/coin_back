package com.example.demo.alert.domain.service;

import com.example.demo.alert.domain.domain.AlertRule;
import com.example.demo.alert.domain.signal.MarketSignal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RuleMatcher {
    public boolean matches(AlertRule rule, MarketSignal signal) {
        if (!rule.isActive()) {
            return false;
        }
        if (rule.getTargetType() != signal.targetType()) {
            return false;
        }
        if (!rule.getAssetSymbol().equalsIgnoreCase(signal.assetSymbol())) {
            return false;
        }
        return rule.getCondition().matches(signal.value());
    }
}
