package com.example.demo.analystics.domain.domain.key;

import com.example.demo.analystics.domain.domain.indicator.TradeIndicatorType;

public record IndicatorKey (
    int period,
    TradeIndicatorType type
) implements DataKey<IndicatorKey>{

    @Override
    public boolean equals(IndicatorKey indicatorKey) {
        return period == indicatorKey.period() && type == indicatorKey.type;
    }
}
