package com.example.demo.alert.domain.service;

import com.example.demo.alert.domain.domain.AlertEvaluationResult;
import com.example.demo.alert.domain.domain.AlertMetric;
import com.example.demo.alert.domain.domain.AlertOperator;
import com.example.demo.alert.domain.domain.AlertRule;
import com.example.demo.alert.domain.domain.TargetType;
import com.example.demo.contracts.message.price_value.PremiumMessage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class AlertEvaluator {

    public List<AlertEvaluationResult> evaluate(AlertRule rule, PremiumMessage message) {
        if (rule.getTargetType() != TargetType.PREMIUM) {
            return List.of();
        }
        if (!rule.getAssetSymbol().equalsIgnoreCase(message.symbol())) {
            return List.of();
        }
        AlertOperator operator = AlertOperator.fromDomain(rule.getCondition().operator());
        BigDecimal threshold = rule.getCondition().threshold();

        List<AlertEvaluationResult> results = new ArrayList<>(AlertMetric.values().length);
        for (AlertMetric metric : AlertMetric.values()) {
            BigDecimal observed = observedValueFor(metric, message);
            if (observed == null) {
                continue;
            }
            boolean matched = operator.apply(observed, threshold);
            results.add(new AlertEvaluationResult(matched, metric, observed, threshold));
        }
        return List.copyOf(results);
    }

    private BigDecimal observedValueFor(AlertMetric metric, PremiumMessage message) {
        return switch (metric) {
            case BUY_PREMIUM_RATE -> message.bid();
            case SELL_PREMIUM_RATE -> message.ask();
        };
    }
}
