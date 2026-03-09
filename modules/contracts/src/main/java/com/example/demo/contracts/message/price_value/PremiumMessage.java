package com.example.demo.contracts.message.price_value;

import java.math.BigDecimal;


public record PremiumMessage(
        String symbol,

        Long baseExchangeId,
        Long compareExchangeId,


        BigDecimal bid,
        BigDecimal ask,

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
