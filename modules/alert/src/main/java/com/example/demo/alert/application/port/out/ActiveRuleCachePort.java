package com.example.demo.alert.application.port.out;

import com.example.demo.alert.domain.domain.AlertRule;
import com.example.demo.alert.domain.domain.TargetType;

import java.util.Collection;
import java.util.List;

public interface ActiveRuleCachePort {
    void warmUp(Collection<AlertRule> rules);

    void invalidate();

    List<AlertRule> findByTargetAndAsset(TargetType targetType, String assetSymbol);
}
