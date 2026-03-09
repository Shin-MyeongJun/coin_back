package com.example.demo.market_data.domain.domain;

import java.math.BigDecimal;

public record PriceValue(
        BigDecimal bid,
        BigDecimal ask,
        Long timestamp
) {
}
