package com.example.demo.contracts.message.candle;

public record PremiumDetailCandleMessage(
        String symbol,
        String baseExchangeId,
        String compareExchangeId,
        String interval,
        String openBaseVal,
        String openBaseQuoteVal,
        String openCompareVal,
        String openCompareQuoteVal,
        String highBaseVal,
        String highBaseQuoteVal,
        String highCompareVal,
        String highCompareQuoteVal,
        String lowBaseVal,
        String lowBaseQuoteVal,
        String lowCompareVal,
        String lowCompareQuoteVal,
        String closeBaseVal,
        String closeBaseQuoteVal,
        String closeCompareVal,
        String closeCompareQuoteVal,
        String bucketOpenTs,
        String bucketCloseTs,
        String observeOpenTs,
        String observeCloseTs
) implements CandleMessage {

    public String extractKey() {
        return symbol + ":" + baseExchangeId + ":" + compareExchangeId + ":" + interval;
    }
}
