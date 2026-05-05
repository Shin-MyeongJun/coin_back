package com.example.demo.analytics_query.application.dto;

import java.math.BigDecimal;

public record IndicatorView(
        Long id,
        Long marketCodeId,
        String symbol,
        Long baseExchangeId,
        Long compareExchangeId,
        String interval,
        String type,
        Integer period,
        BigDecimal value,
        Long bucketOpenTs,
        Long bucketCloseTs,
        Long observeOpenTs,
        Long observeCloseTs
) {
}
