package com.example.demo.market_data_query.application.dto;

import java.math.BigDecimal;

public record FxView(
        Long id,
        String baseCurrency,
        String quoteCurrency,
        BigDecimal rate,
        Long timestamp
) {
}
