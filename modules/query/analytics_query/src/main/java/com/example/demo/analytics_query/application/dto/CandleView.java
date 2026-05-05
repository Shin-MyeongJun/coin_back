package com.example.demo.analytics_query.application.dto;

public sealed interface CandleView
        permits TickCandleView, PremiumCandleView, PremiumDetailCandleView {

    String interval();
    Long bucketOpenTs();
    Long bucketCloseTs();
    Long observeOpenTs();
    Long observeCloseTs();
}
