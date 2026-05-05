package com.example.demo.analytics_query.application.dto;

import java.math.BigDecimal;

public record PremiumScreenerResult(
        String symbol,
        Long baseExchangeId,
        Long compareExchangeId,
        String interval,
        String type,
        Integer period,
        BigDecimal value,
        Long bucketCloseTs
) implements ScreenerResult {
}
