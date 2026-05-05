package com.example.demo.meta_data_query.application.dto;

public record MarketCodeSearchResult(
        Long id,
        Long exchangeId,
        String tradingPair,
        String base,
        String quote
) {
}
