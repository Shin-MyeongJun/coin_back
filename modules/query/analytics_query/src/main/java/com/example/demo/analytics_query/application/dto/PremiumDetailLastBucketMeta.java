package com.example.demo.analytics_query.application.dto;

public record PremiumDetailLastBucketMeta(
        String symbol,
        Long baseExchangeId,
        Long compareExchangeId,
        String interval,
        Long bucketCloseTs
) implements LastBucketMeta {
}
