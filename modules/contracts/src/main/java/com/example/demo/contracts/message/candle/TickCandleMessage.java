package com.example.demo.contracts.message.candle;

public record TickCandleMessage(
        String marketCodeId,
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
        return marketCodeId + ":" + interval;
    }
}
