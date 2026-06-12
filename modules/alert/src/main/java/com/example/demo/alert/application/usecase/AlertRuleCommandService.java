package com.example.demo.alert.application.usecase;

import com.example.demo.alert.application.port.in.DeleteAlertRuleUseCase;
import com.example.demo.alert.application.port.in.RegisterAlertRuleUseCase;
import com.example.demo.alert.application.port.in.ToggleAlertRuleUseCase;
import com.example.demo.alert.application.port.in.UpdateAlertRuleUseCase;
import com.example.demo.alert.application.port.out.ActiveRuleCachePort;
import com.example.demo.alert.application.port.out.DeleteAlertRulePort;
import com.example.demo.alert.application.port.out.LoadAlertRulePort;
import com.example.demo.alert.application.port.out.SaveAlertRulePort;
import com.example.demo.alert.domain.domain.AlertRule;
import com.example.demo.alert.domain.exception.AlertRuleNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class AlertRuleCommandService implements RegisterAlertRuleUseCase, UpdateAlertRuleUseCase,
        DeleteAlertRuleUseCase, ToggleAlertRuleUseCase {
    private final LoadAlertRulePort loadAlertRulePort;
    private final SaveAlertRulePort saveAlertRulePort;
    private final DeleteAlertRulePort deleteAlertRulePort;
    private final ActiveRuleCachePort activeRuleCachePort;
    private final Clock clock;

    @Override
    @Transactional
    public AlertRule register(String userId, AlertRuleCommand command) {
        long now = clock.millis();
        AlertRule rule = new AlertRule(
                null,
                userId,
                command.label(),
                command.targetType(),
                command.assetSymbol(),
                command.condition(),
                command.cooldownSec(),
                command.channels(),
                command.active() == null || command.active(),
                now,
                now
        );
        AlertRule saved = saveAlertRulePort.save(rule);
        activeRuleCachePort.invalidate();
        return saved;
    }

    @Override
    @Transactional
    public AlertRule update(String userId, long id, AlertRuleCommand command) {
        AlertRule current = loadAlertRulePort.findByIdForUser(id, userId)
                .orElseThrow(() -> new AlertRuleNotFoundException(id));
        AlertRule next = new AlertRule(
                current.getId(),
                current.getUserId(),
                command.label(),
                command.targetType(),
                command.assetSymbol(),
                command.condition(),
                command.cooldownSec(),
                command.channels(),
                command.active() == null ? current.isActive() : command.active(),
                current.getCreatedAt(),
                clock.millis()
        );
        AlertRule saved = saveAlertRulePort.save(next);
        activeRuleCachePort.invalidate();
        return saved;
    }

    @Override
    @Transactional
    public void delete(String userId, long id) {
        loadAlertRulePort.findByIdForUser(id, userId)
                .orElseThrow(() -> new AlertRuleNotFoundException(id));
        deleteAlertRulePort.deleteById(id);
        activeRuleCachePort.invalidate();
    }

    @Override
    @Transactional
    public AlertRule toggle(String userId, long id) {
        AlertRule current = loadAlertRulePort.findByIdForUser(id, userId)
                .orElseThrow(() -> new AlertRuleNotFoundException(id));
        current.toggleActive();
        AlertRule saved = saveAlertRulePort.save(current.withUpdatedAt(clock.millis()));
        activeRuleCachePort.invalidate();
        return saved;
    }
}
