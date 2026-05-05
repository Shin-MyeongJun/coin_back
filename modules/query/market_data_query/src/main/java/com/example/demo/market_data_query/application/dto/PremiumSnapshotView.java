package com.example.demo.market_data_query.application.dto;

import java.math.BigDecimal;

public record PremiumSnapshotView(
        String symbol,
        Long baseExchangeId,
        Long compareExchangeId,
        Long timestamp,
        BigDecimal bid,
        BigDecimal ask
) {
}
