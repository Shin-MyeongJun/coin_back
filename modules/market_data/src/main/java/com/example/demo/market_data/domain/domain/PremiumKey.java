package com.example.demo.market_data.domain.domain;

public record PremiumKey(
        String symbol,
        Long baseExchangeId,
        Long compareExchangeId
) {
}
