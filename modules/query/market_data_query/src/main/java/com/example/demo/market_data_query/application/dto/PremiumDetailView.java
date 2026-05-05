package com.example.demo.market_data_query.application.dto;

import java.math.BigDecimal;

public record PremiumDetailView(
        Long id,
        String symbol,
        Long baseExchangeId,
        Long compareExchangeId,
        BigDecimal baseBid,
        BigDecimal baseAsk,
        BigDecimal baseQuoteVal,
        BigDecimal compareBid,
        BigDecimal compareAsk,
        BigDecimal compareQuoteVal,
        Long timestamp
) {
}
