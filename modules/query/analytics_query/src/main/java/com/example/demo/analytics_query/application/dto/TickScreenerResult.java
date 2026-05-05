package com.example.demo.analytics_query.application.dto;

import java.math.BigDecimal;

public record TickScreenerResult(
        Long marketCodeId,
        String interval,
        String type,
        Integer period,
        BigDecimal value,
        Long bucketCloseTs
) implements ScreenerResult {
}
