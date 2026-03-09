package com.example.demo.analystics.domain.domain.candle.close;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.TimeWindow;

import java.math.BigDecimal;

public record PremiumCloseCandle(
        String symbol,
        Long baseExchangeId,
        Long compareExchangeId,
        Interval interval,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        TimeWindow times
) implements CloseCandle {
}
