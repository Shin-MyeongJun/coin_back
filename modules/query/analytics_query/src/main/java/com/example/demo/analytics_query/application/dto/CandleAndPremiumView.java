package com.example.demo.analytics_query.application.dto;

import java.math.BigDecimal;

public record CandleAndPremiumView(
        Long marketCodeId,
        String interval,
        BigDecimal candleOpen,
        BigDecimal candleHigh,
        BigDecimal candleLow,
        BigDecimal candleClose,
        BigDecimal premiumOpen,
        BigDecimal premiumHigh,
        BigDecimal premiumLow,
        BigDecimal premiumClose,
        Long bucketOpenTs,
        Long bucketCloseTs
) {
}
