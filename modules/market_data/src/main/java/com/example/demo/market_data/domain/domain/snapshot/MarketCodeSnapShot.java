package com.example.demo.market_data.domain.domain.snapshot;

public record MarketCodeSnapShot(
        Long id,
        Long exchangeId,
        String baseAsset,
        String tradingPair
) {
}
