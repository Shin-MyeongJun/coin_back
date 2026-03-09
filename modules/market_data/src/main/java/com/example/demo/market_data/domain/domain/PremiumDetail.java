package com.example.demo.market_data.domain.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record PremiumDetail(
        String symbol,
        Long baseExchangeId,
        Long compareExchangeId,

        BigDecimal baseBid,
        BigDecimal baseAsk,
        BigDecimal baseQuoteVal,

        BigDecimal compareBid,
        BigDecimal compareAsk,
        BigDecimal compareQuoteVal,

        long timestamp
) implements BidAskQuote
{
    @Override
    public BigDecimal bid() {

        BigDecimal base = baseAsk.multiply(baseQuoteVal);
        BigDecimal compare = compareBid.multiply(compareQuoteVal);


        return compare.divide(base,8, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100));
    }

    @Override
    public BigDecimal ask() {

        BigDecimal base = baseBid.multiply(baseQuoteVal);
        BigDecimal compare = compareAsk.multiply(compareQuoteVal);

        return compare.divide(base,8, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100));
    }
}
