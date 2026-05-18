package com.example.demo.alert.application.usecase;

import com.example.demo.alert.application.port.in.QueryAlertRuleUseCase;
import com.example.demo.alert.application.port.out.LoadAlertRulePort;
import com.example.demo.alert.domain.domain.AlertRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AlertRuleQueryService implements QueryAlertRuleUseCase {
    private final LoadAlertRulePort loadAlertRulePort;

    @Override
    @Transactional(readOnly = true)
    public AlertRule get(String userId, long id) {
        return loadAlertRulePort.findByIdForUser(id, userId)
                .orElseThrow(() -> new NoSuchElementException("alert rule not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public AlertRulePage findByUser(String userId, int page, int size) {
        return loadAlertRulePort.findByUser(userId, page, size);
    }
}
