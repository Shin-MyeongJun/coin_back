package com.example.demo.contracts.message.candle;

public record PremiumCandleMessage(
        String symbol,
        String baseExchangeId,
        String compareExchangeId,
        String interval,
        String open,
        String high,
        String low,
        String close,
        String bucketOpenTs,
        String bucketCloseTs,
        String observeOpenTs,
        String observeCloseTs
) implements CandleMessage {

    public String extractKey() {
        return symbol + ":" + baseExchangeId + ":" + compareExchangeId + ":" + interval;
    }
}
