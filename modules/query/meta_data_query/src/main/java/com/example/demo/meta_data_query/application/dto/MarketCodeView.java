package com.example.demo.meta_data_query.application.dto;

public record MarketCodeView(
        Long id,
        Long exchangeId,
        String base,
        String quote,
        String tradingPair
) {
}
