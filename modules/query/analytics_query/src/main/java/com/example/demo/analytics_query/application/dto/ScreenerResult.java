package com.example.demo.analytics_query.application.dto;

import java.math.BigDecimal;

public record ScreenerResult(
        Long marketCodeId,
        String interval,
        String type,
        Integer period,
        BigDecimal value,
        Long bucketCloseTs
) {
}
