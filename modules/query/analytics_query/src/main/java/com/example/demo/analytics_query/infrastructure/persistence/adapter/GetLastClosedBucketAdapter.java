package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.analytics_query.application.dto.LastBucketMeta;
import com.example.demo.analytics_query.application.port.out.GetLastClosedBucketPort;
import com.example.demo.analytics_query.infrastructure.persistence.repo.PremiumCandleJpaRepository;
import com.example.demo.analytics_query.infrastructure.persistence.repo.TickCandleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetLastClosedBucketAdapter implements GetLastClosedBucketPort {

    private final TickCandleJpaRepository tickRepo;
    private final PremiumCandleJpaRepository premiumRepo;

    @Override
    public Optional<LastBucketMeta> findLastTickCandle(Long marketCodeId, String interval) {
        return tickRepo.findMaxBucketCloseTs(marketCodeId, interval)
                .map(ts -> new LastBucketMeta(interval, ts));
    }

    @Override
    public Optional<LastBucketMeta> findLastPremiumCandle(String symbol, Long baseExchangeId, Long compareExchangeId, String interval) {
        return premiumRepo.findMaxBucketCloseTs(symbol, baseExchangeId, compareExchangeId, interval)
                .map(ts -> new LastBucketMeta(interval, ts));
    }
}
