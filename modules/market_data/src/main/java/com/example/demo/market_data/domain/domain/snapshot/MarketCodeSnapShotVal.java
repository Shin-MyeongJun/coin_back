package com.example.demo.market_data.domain.domain.snapshot;

public record MarketCodeSnapShotVal(
        Long exchangeId,
        String baseAsset,
        String tradingPair
) {
}
