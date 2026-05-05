package com.example.demo.analytics_query.application.dto;

public record TickLastBucketMeta(
        Long marketCodeId,
        String interval,
        Long bucketCloseTs
) implements LastBucketMeta {
}
