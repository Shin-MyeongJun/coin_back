package com.example.demo.market_data.domain.domain;

import java.math.BigDecimal;

public record Fx(
        String base,
        String compare,
        BigDecimal val,
        long timestamp
) {
}
