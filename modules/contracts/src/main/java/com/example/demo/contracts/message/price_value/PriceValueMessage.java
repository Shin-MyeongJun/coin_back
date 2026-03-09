package com.example.demo.contracts.message.price_value;

import java.math.BigDecimal;

public record PriceValueMessage(
        BigDecimal bid,
        BigDecimal ask,
        BigDecimal timestamp
){}
