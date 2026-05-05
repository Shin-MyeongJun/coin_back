package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.LastBucketMeta;

import java.util.Optional;

public interface GetLastClosedBucketPort {
    Optional<LastBucketMeta> findLastTickCandle(Long marketCodeId, String interval);
    Optional<LastBucketMeta> findLastPremiumCandle(String symbol, Long baseExchangeId, Long compareExchangeId, String interval);
}
