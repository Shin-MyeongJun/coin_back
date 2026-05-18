package com.example.demo.alert.infrastructure.web.dto;

import com.example.demo.alert.domain.domain.AlertCondition;
import com.example.demo.alert.domain.domain.AlertRule;
import com.example.demo.alert.domain.domain.Channel;
import com.example.demo.alert.domain.domain.TargetType;

import java.util.Set;

public record AlertRuleResponse(
        Long id,
        String userId,
        String label,
        TargetType targetType,
        String assetSymbol,
        AlertCondition condition,
        int cooldownSec,
        Set<Channel> channels,
        boolean active,
        long createdAt,
        long updatedAt
) {
    public static AlertRuleResponse from(AlertRule rule) {
        return new AlertRuleResponse(
                rule.getId(),
                rule.getUserId(),
                rule.getLabel(),
                rule.getTargetType(),
                rule.getAssetSymbol(),
                rule.getCondition(),
                rule.getCooldownSec(),
                rule.getChannels(),
                rule.isActive(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }
}
