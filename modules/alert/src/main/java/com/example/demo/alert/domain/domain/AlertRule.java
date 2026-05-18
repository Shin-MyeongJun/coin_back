package com.example.demo.alert.domain.domain;

import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter
public class AlertRule {
    private final Long id;
    private final String userId;
    private final String label;
    private final TargetType targetType;
    private final String assetSymbol;
    private final AlertCondition condition;
    private final int cooldownSec;
    private final Set<Channel> channels;
    private boolean active;
    private final long createdAt;
    private final long updatedAt;

    public AlertRule(
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
        this.id = id;
        this.userId = requireNonBlank(userId, "userId");
        this.label = validateLabel(label);
        this.targetType = Objects.requireNonNull(targetType, "targetType must not be null");
        this.assetSymbol = requireNonBlank(assetSymbol, "assetSymbol");
        this.condition = Objects.requireNonNull(condition, "condition must not be null");
        if (cooldownSec < 10) {
            throw new IllegalArgumentException("cooldownSec must be at least 10");
        }
        this.cooldownSec = cooldownSec;
        this.channels = validateChannels(channels);
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void toggleActive() {
        this.active = !this.active;
    }

    public AlertRule withUpdatedAt(long newUpdatedAt) {
        return new AlertRule(
                id,
                userId,
                label,
                targetType,
                assetSymbol,
                condition,
                cooldownSec,
                channels,
                active,
                createdAt,
                newUpdatedAt
        );
    }

    private static String validateLabel(String label) {
        String value = requireNonBlank(label, "label");
        if (value.length() > 50) {
            throw new IllegalArgumentException("label must be 50 characters or less");
        }
        return value;
    }

    private static String requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be null or blank");
        }
        return value.trim();
    }

    private static Set<Channel> validateChannels(Set<Channel> channels) {
        if (channels == null || channels.isEmpty()) {
            throw new IllegalArgumentException("channels must contain at least one channel");
        }
        return Collections.unmodifiableSet(new LinkedHashSet<>(channels));
    }
}
