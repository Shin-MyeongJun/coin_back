package com.example.demo.alert.infrastructure.persistence.mapper;

import com.example.demo.alert.domain.domain.AlertCondition;
import com.example.demo.alert.domain.domain.AlertRule;
import com.example.demo.alert.domain.domain.Channel;
import com.example.demo.alert.domain.domain.Operator;
import com.example.demo.alert.domain.domain.TargetType;
import com.example.demo.alert.infrastructure.persistence.entity.AlertRuleEntity;
import com.example.demo.infra_shard.persistence.EntityMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AlertRuleEntityMapper implements EntityMapping<AlertRule, AlertRuleEntity> {
    @Override
    public AlertRuleEntity toEntity(AlertRule domain) {
        return AlertRuleEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .label(domain.getLabel())
                .targetType(domain.getTargetType().name())
                .assetSymbol(domain.getAssetSymbol())
                .operator(domain.getCondition().operator().name())
                .threshold(domain.getCondition().threshold())
                .cooldownSec(domain.getCooldownSec())
                .channels(toChannelText(domain.getChannels()))
                .active(domain.isActive())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    @Override
    public AlertRule toDomain(AlertRuleEntity entity) {
        return new AlertRule(
                entity.getId(),
                entity.getUserId(),
                entity.getLabel(),
                TargetType.valueOf(entity.getTargetType()),
                entity.getAssetSymbol(),
                new AlertCondition(Operator.valueOf(entity.getOperator()), entity.getThreshold()),
                entity.getCooldownSec(),
                toChannels(entity.getChannels()),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private String toChannelText(Set<Channel> channels) {
        return channels.stream()
                .sorted(Comparator.comparing(Enum::name))
                .map(Channel::name)
                .collect(Collectors.joining(","));
    }

    private Set<Channel> toChannels(String channels) {
        return Arrays.stream(channels.split(","))
                .filter(value -> !value.isBlank())
                .map(String::trim)
                .map(Channel::valueOf)
                .collect(Collectors.toUnmodifiableSet());
    }
}
