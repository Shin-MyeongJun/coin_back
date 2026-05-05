package com.example.demo.economic_query.application.dto;

import java.math.BigDecimal;

public record CorrelationResultView(
        Long id,
        String assetSymbol,
        String indicatorCode,
        BigDecimal correlation,
        Integer periodDays,
        Long calculatedAt
) {
}
