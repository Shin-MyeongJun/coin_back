package com.example.demo.contracts.message.price_value;



import java.math.BigDecimal;


public record PremiumDetailMessage(
        String symbol,

        Long baseExchangeId,
        Long compareExchangeId,

        BigDecimal baseBid,
        BigDecimal baseAsk,
        BigDecimal baseQuoteVal,

        BigDecimal compareBid,
        BigDecimal compareAsk,
        BigDecimal compareQuoteVal,

        Long timestamp

        ) {
        public String extractKey() {
                return String.join("_",
                        symbol,
                        baseExchangeId.toString(),
                        compareExchangeId.toString()
                );
        }
}
