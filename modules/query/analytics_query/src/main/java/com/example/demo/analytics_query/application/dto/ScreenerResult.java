package com.example.demo.analytics_query.application.dto;

public sealed interface ScreenerResult
        permits TickScreenerResult, PremiumScreenerResult {

    String interval();
    String type();
    Long bucketCloseTs();
}
