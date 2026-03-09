package com.example.demo.ingestion.exchange.domain;

public record MarketCodeKey(
        String exchange,
        String tradingPair
) {}
