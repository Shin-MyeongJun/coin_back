package com.example.demo.analystics.domain.domain.candle.close;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.TimeWindow;
import com.example.demo.analystics.domain.domain.candle.value.PremiumDetailValue;

public record PremiumDetailCloseCandle (
    String symbol,
    Long baseExchangeId,
    Long compareExchangeId,
    Interval interval,
    PremiumDetailValue open,
    PremiumDetailValue high,
    PremiumDetailValue low,
    PremiumDetailValue close,
    TimeWindow times
)implements CloseCandle {}
