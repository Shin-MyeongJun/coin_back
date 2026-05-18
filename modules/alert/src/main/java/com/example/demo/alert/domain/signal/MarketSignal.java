package com.example.demo.alert.domain.signal;

import com.example.demo.alert.domain.domain.TargetType;

import java.math.BigDecimal;

public sealed interface MarketSignal
        permits TickSignal, PremiumSignal, PremiumDetailSignal, IndicatorSignal {
    String assetSymbol();

    BigDecimal value();

    long observedAt();

    TargetType targetType();
}
