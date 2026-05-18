package com.example.demo.alert.domain.signal;

import com.example.demo.alert.domain.domain.TargetType;

import java.math.BigDecimal;

public record PremiumDetailSignal(
        String assetSymbol,
        BigDecimal value,
        long observedAt,
        Long baseExchangeId,
        Long compareExchangeId,
        BigDecimal baseBid,
        BigDecimal baseAsk,
        BigDecimal compareBid,
        BigDecimal compareAsk
) implements MarketSignal {
    @Override
    public TargetType targetType() {
        return TargetType.PREMIUM;
    }
}
