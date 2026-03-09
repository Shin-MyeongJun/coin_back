package com.example.demo.ingestion.exchange.domain;

public record MarketCodeValue(
        String exchangeType,
        String quote,
        String base
){
}
