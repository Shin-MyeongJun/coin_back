package com.example.demo.alert.infrastructure.cache;

import com.example.demo.alert.application.port.out.ActiveAlertRuleStorePort;
import com.example.demo.alert.application.port.out.LoadAlertRulePort;
import com.example.demo.alert.domain.domain.AlertRule;
import com.example.demo.alert.domain.domain.TargetType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class InMemoryActiveAlertRuleStore implements ActiveAlertRuleStorePort {

    private final LoadAlertRulePort loadAlertRulePort;
    private final AtomicReference<Map<String, List<AlertRule>>> snapshot =
            new AtomicReference<>(Map.of());

    @PostConstruct
    public void initialize() {
        refresh();
    }

    @Override
    public void refresh() {
        try {
            Map<String, List<AlertRule>> next = loadAlertRulePort.findAllActive().stream()
                    .filter(AlertRule::isActive)
                    .collect(Collectors.groupingBy(this::indexKey));
            snapshot.set(Map.copyOf(next));
            log.debug("active alert rule store refreshed: groups={}", next.size());
        } catch (Exception e) {
            log.warn("active alert rule store refresh failed: {}", e.getMessage());
        }
    }

    @Override
    public List<AlertRule> findActive(TargetType targetType, String assetSymbol) {
        if (targetType == null || assetSymbol == null) {
            return List.of();
        }
        return snapshot.get().getOrDefault(indexKey(targetType, assetSymbol), List.of());
    }

    private String indexKey(AlertRule rule) {
        return indexKey(rule.getTargetType(), rule.getAssetSymbol());
    }

    private String indexKey(TargetType targetType, String assetSymbol) {
        return targetType.name() + ":" + assetSymbol.toUpperCase(Locale.ROOT);
    }
}
