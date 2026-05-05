package com.example.demo.analytics_query.application.dto;

import java.math.BigDecimal;

public record CandleView(
        Long id,
        Long marketCodeId,
        String symbol,
        Long baseExchangeId,
        Long compareExchangeId,
        String interval,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        Long bucketOpenTs,
        Long bucketCloseTs,
        Long observeOpenTs,
        Long observeCloseTs
) {
}
