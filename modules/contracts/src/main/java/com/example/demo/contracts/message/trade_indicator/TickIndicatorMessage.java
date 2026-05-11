package com.example.demo.contracts.message.trade_indicator;

public record TickIndicatorMessage(
        String marketCodeId,
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
        return marketCodeId + ":" + interval + ":" + type + ":" + period;
    }
}
