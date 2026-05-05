package com.example.demo.economic_query.application.dto;

import java.math.BigDecimal;

public record IndicatorSeriesView(
        Long indCodeId,
        BigDecimal value,
        Long observationDate,
        Long releaseDate,
        Long timestamp
) {
}
