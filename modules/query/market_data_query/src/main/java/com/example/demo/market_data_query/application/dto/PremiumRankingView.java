package com.example.demo.market_data_query.application.dto;

import java.math.BigDecimal;

public record PremiumRankingView(
        String symbol,
        Long baseExchangeId,
        Long compareExchangeId,
        BigDecimal bid,
        Integer rank
) {
}
