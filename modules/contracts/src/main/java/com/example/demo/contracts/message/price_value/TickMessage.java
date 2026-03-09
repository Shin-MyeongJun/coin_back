package com.example.demo.contracts.message.price_value;

import java.math.BigDecimal;


public record TickMessage(
        Long marketCodeId,

        BigDecimal bid,
        BigDecimal ask,

        Long timestamp

        ) {
        public String extractKey() {
                return String.join("_",
                        marketCodeId.toString()
                );
        }

}
