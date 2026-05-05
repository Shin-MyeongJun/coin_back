package com.example.demo.economic_query.application.dto;

import java.math.BigDecimal;

public record IndicatorChangeRateView(
        Long observationDate,
        BigDecimal value,
        BigDecimal previousValue,
        BigDecimal changeRate
) {
}
