package com.example.demo.alert.domain.signal;

import com.example.demo.alert.domain.domain.TargetType;

import java.math.BigDecimal;

public record IndicatorSignal(
        String assetSymbol,
        BigDecimal value,
        long observedAt,
        String sourceType,
        String interval,
        String indicatorType,
        String period,
        Long baseExchangeId,
        Long compareExchangeId
) implements MarketSignal {
    @Override
    public TargetType targetType() {
        return TargetType.INDICATOR;
    }
}
