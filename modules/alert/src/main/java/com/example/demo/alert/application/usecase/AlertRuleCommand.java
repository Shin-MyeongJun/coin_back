package com.example.demo.alert.application.usecase;

import com.example.demo.alert.domain.domain.AlertCondition;
import com.example.demo.alert.domain.domain.Channel;
import com.example.demo.alert.domain.domain.TargetType;

import java.util.Set;

public record AlertRuleCommand(
        String label,
        TargetType targetType,
        String assetSymbol,
        AlertCondition condition,
        int cooldownSec,
        Set<Channel> channels,
        Boolean active
) {
}
