package com.example.demo.alert.infrastructure.web.dto;

import com.example.demo.alert.application.usecase.AlertRuleCommand;
import com.example.demo.alert.domain.domain.AlertCondition;
import com.example.demo.alert.domain.domain.Channel;
import com.example.demo.alert.domain.domain.Operator;
import com.example.demo.alert.domain.domain.TargetType;
import com.example.demo.alert.infrastructure.config.AlertProperties;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;

public record AlertRuleRequest(
        @NotBlank
        @Size(max = 50)
        String label,

        @NotNull
        TargetType targetType,

        @NotBlank
        @Size(max = 16)
        String assetSymbol,

        @NotNull
        Operator operator,

        @NotNull
        @DecimalMin(value = "-999999999999.99999999")
        @DecimalMax(value = "999999999999.99999999")
        BigDecimal threshold,

        @Min(10)
        int cooldownSec,

        Set<Channel> channels,

        Boolean active
) {
    public AlertRuleCommand toCommand(AlertProperties properties) {
        Set<Channel> normalizedChannels = resolveChannels(properties);
        return new AlertRuleCommand(
                label,
                targetType,
                assetSymbol,
                new AlertCondition(operator, threshold),
                cooldownSec,
                normalizedChannels,
                active
        );
    }

    private Set<Channel> resolveChannels(AlertProperties properties) {
        if (channels != null && !channels.isEmpty()) {
            return Set.copyOf(channels);
        }
        if (properties != null && properties.defaultChannels() != null && !properties.defaultChannels().isEmpty()) {
            return Set.copyOf(properties.defaultChannels());
        }
        return Set.of(Channel.SSE);
    }
}
