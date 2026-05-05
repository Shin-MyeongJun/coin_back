package com.example.demo.market_data_query.application.dto;

import java.math.BigDecimal;

public record TickLatestView(
        Long id,
        Long marketCodeId,
        Long timestamp,
        BigDecimal bid,
        BigDecimal ask
) {
}
