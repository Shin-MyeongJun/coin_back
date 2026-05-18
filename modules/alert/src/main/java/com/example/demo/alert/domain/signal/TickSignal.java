package com.example.demo.alert.domain.signal;

import com.example.demo.alert.domain.domain.TargetType;

import java.math.BigDecimal;

public record TickSignal(
        String assetSymbol,
        BigDecimal value,
        long observedAt,
        Long marketCodeId
) implements MarketSignal {
    @Override
    public TargetType targetType() {
        return TargetType.TICK;
    }
}
