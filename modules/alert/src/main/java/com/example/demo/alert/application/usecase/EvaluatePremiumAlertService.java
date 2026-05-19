package com.example.demo.alert.application.usecase;

import com.example.demo.alert.application.port.in.EvaluatePremiumAlertUseCase;
import com.example.demo.alert.application.port.out.ActiveAlertRuleStorePort;
import com.example.demo.alert.application.port.out.SaveAlertFiringPort;
import com.example.demo.alert.domain.domain.AlertEvaluationResult;
import com.example.demo.alert.domain.domain.AlertFiring;
import com.example.demo.alert.domain.domain.AlertRule;
import com.example.demo.alert.domain.domain.TargetType;
import com.example.demo.alert.domain.service.AlertCooldownPolicy;
import com.example.demo.alert.domain.service.AlertEvaluator;
import com.example.demo.contracts.message.price_value.PremiumMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@RequiredArgsConstructor
public class EvaluatePremiumAlertService implements EvaluatePremiumAlertUseCase {

    private final ActiveAlertRuleStorePort activeAlertRuleStorePort;
    private final AlertEvaluator alertEvaluator;
    private final AlertCooldownPolicy alertCooldownPolicy;
    private final SaveAlertFiringPort saveAlertFiringPort;
    private final Clock clock;
    private final ConcurrentMap<Long, Long> lastFiredAtByRuleId = new ConcurrentHashMap<>();

    @Override
    public void evaluate(PremiumMessage message) {
        if (message == null || message.symbol() == null) {
            return;
        }
        long now = clock.millis();
        List<AlertRule> rules = activeAlertRuleStorePort.findActive(TargetType.PREMIUM, message.symbol());
        for (AlertRule rule : rules) {
            evaluateRule(rule, message, now);
        }
    }

    private void evaluateRule(AlertRule rule, PremiumMessage message, long now) {
        List<AlertEvaluationResult> results = alertEvaluator.evaluate(rule, message);
        for (AlertEvaluationResult result : results) {
            if (!result.matched()) {
                continue;
            }
            Long lastFiredAt = lastFiredAtByRuleId.get(rule.getId());
            if (!alertCooldownPolicy.isAllowed(lastFiredAt, rule.getCooldownSec(), now)) {
                return;
            }
            lastFiredAtByRuleId.put(rule.getId(), now);
            saveAlertFiringPort.save(toFiring(rule, result, message, now));
            return;
        }
    }

    private AlertFiring toFiring(AlertRule rule, AlertEvaluationResult result, PremiumMessage message, long now) {
        String conditionText = result.metric().name()
                + " " + rule.getCondition().asText()
                + " (symbol=" + message.symbol() + ")";
        return new AlertFiring(
                null,
                rule.getId(),
                rule.getUserId(),
                rule.getLabel(),
                conditionText,
                result.observedValue(),
                now
        );
    }
}
