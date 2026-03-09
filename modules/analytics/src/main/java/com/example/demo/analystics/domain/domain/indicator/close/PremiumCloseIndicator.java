package com.example.demo.analystics.domain.domain.indicator.close;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.TimeWindow;
import com.example.demo.analystics.domain.domain.indicator.TradeIndicatorType;

import java.math.BigDecimal;

public record PremiumCloseIndicator(
        String symbol,
        Long baseExchangeId,
        Long compareExchangeId,
        Interval interval,
        TradeIndicatorType type,
        int period,
        BigDecimal value,
        TimeWindow times
) {
}
