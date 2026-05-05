package com.example.demo.analytics_query.application.dto;

import java.math.BigDecimal;

public record ScreenerCondition(
        String interval,
        String type,
        Integer period,
        BigDecimal minValue,
        BigDecimal maxValue
) {
}
