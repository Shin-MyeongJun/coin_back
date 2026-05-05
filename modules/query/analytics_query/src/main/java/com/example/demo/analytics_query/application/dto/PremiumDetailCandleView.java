package com.example.demo.analytics_query.application.dto;

import java.math.BigDecimal;

public record PremiumDetailCandleView(
        String symbol,
        Long baseExchangeId,
        Long compareExchangeId,
        String interval,
        PremiumDetailOHLC open,
        PremiumDetailOHLC high,
        PremiumDetailOHLC low,
        PremiumDetailOHLC close,
        Long bucketOpenTs,
        Long bucketCloseTs,
        Long observeOpenTs,
        Long observeCloseTs
) implements CandleView {

    public record PremiumDetailOHLC(
            BigDecimal basePrice,
            BigDecimal baseQuoteVal,
            BigDecimal comparePrice,
            BigDecimal compareQuoteVal
    ) {
    }
}
