package com.example.demo.analytics_query.application.dto;

import java.math.BigDecimal;

public record TickIndicatorView(
        Long marketCodeId,
        String interval,
        String type,
        Integer period,
        BigDecimal value,
        Long bucketOpenTs,
        Long bucketCloseTs,
        Long observeOpenTs,
        Long observeCloseTs
) implements IndicatorView {
}
