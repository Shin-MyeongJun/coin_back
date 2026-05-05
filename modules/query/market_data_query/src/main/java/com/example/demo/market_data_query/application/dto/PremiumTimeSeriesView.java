package com.example.demo.market_data_query.application.dto;

import java.math.BigDecimal;

public record PremiumTimeSeriesView(
        String symbol,
        Long baseExchangeId,
        Long compareExchangeId,
        Long bucketTs,
        BigDecimal bid,
        BigDecimal ask
) {
}
