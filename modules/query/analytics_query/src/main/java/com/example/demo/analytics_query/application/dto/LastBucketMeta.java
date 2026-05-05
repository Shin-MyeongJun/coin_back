package com.example.demo.analytics_query.application.dto;

public sealed interface LastBucketMeta
        permits TickLastBucketMeta, PremiumLastBucketMeta, PremiumDetailLastBucketMeta {

    String interval();
    Long bucketCloseTs();
}
