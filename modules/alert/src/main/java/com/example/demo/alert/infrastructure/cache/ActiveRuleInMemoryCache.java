package com.example.demo.alert.infrastructure.cache;

import com.example.demo.alert.application.port.out.ActiveRuleCachePort;
import com.example.demo.alert.application.port.out.LoadAlertRulePort;
import com.example.demo.alert.domain.domain.AlertRule;
import com.example.demo.alert.domain.domain.TargetType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
public class ActiveRuleInMemoryCache implements ActiveRuleCachePort {
    private final LoadAlertRulePort loadAlertRulePort;
    private final Map<String, List<AlertRule>> rulesByTargetAsset = new ConcurrentHashMap<>();
    private final AtomicBoolean loaded = new AtomicBoolean(false);

    @PostConstruct
    public void initialize() {
        warmUp(loadAlertRulePort.findAllActive());
    }

    @Override
    public void warmUp(Collection<AlertRule> rules) {
        Map<String, List<AlertRule>> next = rules.stream()
                .filter(AlertRule::isActive)
                .collect(java.util.stream.Collectors.groupingBy(
                        rule -> cacheKey(rule.getTargetType(), rule.getAssetSymbol())
                ));
        rulesByTargetAsset.clear();
        rulesByTargetAsset.putAll(next);
        loaded.set(true);
    }

    @Override
    public void invalidate() {
        rulesByTargetAsset.clear();
        loaded.set(false);
    }

    @Override
    public List<AlertRule> findByTargetAndAsset(TargetType targetType, String assetSymbol) {
        if (!loaded.get()) {
            warmUp(loadAlertRulePort.findAllActive());
        }
        return rulesByTargetAsset.getOrDefault(cacheKey(targetType, assetSymbol), List.of());
    }

    private String cacheKey(TargetType targetType, String assetSymbol) {
        return targetType.name() + ":" + assetSymbol.toUpperCase(java.util.Locale.ROOT);
    }
}
