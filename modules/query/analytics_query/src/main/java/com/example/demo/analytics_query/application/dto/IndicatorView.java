package com.example.demo.analytics_query.application.dto;

import java.math.BigDecimal;

public sealed interface IndicatorView
        permits TickIndicatorView, PremiumIndicatorView {

    String interval();
    String type();
    Integer period();
    BigDecimal value();
    Long bucketOpenTs();
    Long bucketCloseTs();
    Long observeOpenTs();
    Long observeCloseTs();
}
