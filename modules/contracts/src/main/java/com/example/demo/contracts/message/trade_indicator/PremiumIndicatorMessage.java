package com.example.demo.contracts.message.trade_indicator;

public record PremiumIndicatorMessage(
        String symbol,
        String baseExchangeId,
        String compareExchangeId,
        String interval,
        String type,
        String period,
        String value,
        String bucketOpenTs,
        String bucketCloseTs,
        String observeOpenTs,
        String observeCloseTs
) implements TradeIndicatorMessage {

    public String extractKey() {
        return symbol + ":" + baseExchangeId + ":" + compareExchangeId + ":" + interval + ":" + type + ":" + period;
    }
}
