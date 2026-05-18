package com.example.demo.alert.infrastructure.messaging.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class SignalValueSupport {
    private SignalValueSupport() {
    }

    static BigDecimal midpoint(BigDecimal bid, BigDecimal ask) {
        if (bid == null && ask == null) {
            return BigDecimal.ZERO;
        }
        if (bid == null) {
            return ask;
        }
        if (ask == null) {
            return bid;
        }
        return bid.add(ask).divide(BigDecimal.valueOf(2), 8, RoundingMode.HALF_UP);
    }

    static long parseLongOrZero(String value) {
        if (value == null || value.isBlank()) {
            return 0L;
        }
        return Long.parseLong(value);
    }

    static BigDecimal parseDecimalOrZero(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value);
    }

    static BigDecimal premiumDetailValue(
            BigDecimal baseBid,
            BigDecimal baseAsk,
            BigDecimal baseQuoteVal,
            BigDecimal compareBid,
            BigDecimal compareAsk,
            BigDecimal compareQuoteVal
    ) {
        BigDecimal bid = premiumDetailBid(baseAsk, baseQuoteVal, compareBid, compareQuoteVal);
        BigDecimal ask = premiumDetailAsk(baseBid, baseQuoteVal, compareAsk, compareQuoteVal);
        return midpoint(bid, ask);
    }

    private static BigDecimal premiumDetailBid(
            BigDecimal baseAsk,
            BigDecimal baseQuoteVal,
            BigDecimal compareBid,
            BigDecimal compareQuoteVal
    ) {
        return premiumRatio(baseAsk, baseQuoteVal, compareBid, compareQuoteVal);
    }

    private static BigDecimal premiumDetailAsk(
            BigDecimal baseBid,
            BigDecimal baseQuoteVal,
            BigDecimal compareAsk,
            BigDecimal compareQuoteVal
    ) {
        return premiumRatio(baseBid, baseQuoteVal, compareAsk, compareQuoteVal);
    }

    private static BigDecimal premiumRatio(
            BigDecimal basePrice,
            BigDecimal baseQuoteVal,
            BigDecimal comparePrice,
            BigDecimal compareQuoteVal
    ) {
        if (basePrice == null || baseQuoteVal == null || comparePrice == null || compareQuoteVal == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal base = basePrice.multiply(baseQuoteVal);
        if (base.signum() == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal compare = comparePrice.multiply(compareQuoteVal);
        return compare.divide(base, 8, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100));
    }
}
