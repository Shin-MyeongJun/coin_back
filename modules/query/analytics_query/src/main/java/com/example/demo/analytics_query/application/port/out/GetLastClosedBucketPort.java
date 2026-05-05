package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.PremiumDetailLastBucketMeta;
import com.example.demo.analytics_query.application.dto.PremiumLastBucketMeta;
import com.example.demo.analytics_query.application.dto.TickLastBucketMeta;

import java.util.Optional;

public interface GetLastClosedBucketPort {
    Optional<TickLastBucketMeta> findLastTick(Long marketCodeId, String interval);
    Optional<PremiumLastBucketMeta> findLastPremium(String symbol, Long baseExchangeId, Long compareExchangeId, String interval);
    Optional<PremiumDetailLastBucketMeta> findLastPremiumDetail(String symbol, Long baseExchangeId, Long compareExchangeId, String interval);
}
