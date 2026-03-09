package com.example.demo.contracts.message.meta;

public record MarketCodeMessage(
        Long id,
        Long exchangeId,
        String base,
        String tradingPair
) {
}

