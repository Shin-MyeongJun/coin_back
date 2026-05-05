package com.example.demo.analytics_query.application.dto;

public record LastBucketMeta(
        String interval,
        Long bucketCloseTs
) {
}
