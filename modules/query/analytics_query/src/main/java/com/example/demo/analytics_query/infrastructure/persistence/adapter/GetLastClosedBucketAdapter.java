package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.analytics_query.application.dto.PremiumDetailLastBucketMeta;
import com.example.demo.analytics_query.application.dto.PremiumLastBucketMeta;
import com.example.demo.analytics_query.application.dto.TickLastBucketMeta;
import com.example.demo.analytics_query.application.port.out.GetLastClosedBucketPort;
import com.example.demo.analytics_query.infrastructure.persistence.repo.PremiumCandleJpaRepository;
import com.example.demo.analytics_query.infrastructure.persistence.repo.PremiumDetailCandleJpaRepository;
import com.example.demo.analytics_query.infrastructure.persistence.repo.TickCandleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetLastClosedBucketAdapter implements GetLastClosedBucketPort {

    private final TickCandleJpaRepository tickRepo;
    private final PremiumCandleJpaRepository premiumRepo;
    private final PremiumDetailCandleJpaRepository premiumDetailRepo;

    @Override
    public Optional<TickLastBucketMeta> findLastTick(Long marketCodeId, String interval) {
        return tickRepo.findMaxBucketCloseTs(marketCodeId, interval)
                .map(ts -> new TickLastBucketMeta(marketCodeId, interval, ts));
    }

    @Override
    public Optional<PremiumLastBucketMeta> findLastPremium(String symbol, Long baseExchangeId, Long compareExchangeId, String interval) {
        return premiumRepo.findMaxBucketCloseTs(symbol, baseExchangeId, compareExchangeId, interval)
                .map(ts -> new PremiumLastBucketMeta(symbol, baseExchangeId, compareExchangeId, interval, ts));
    }

    @Override
    public Optional<PremiumDetailLastBucketMeta> findLastPremiumDetail(String symbol, Long baseExchangeId, Long compareExchangeId, String interval) {
        return premiumDetailRepo.findMaxBucketCloseTs(symbol, baseExchangeId, compareExchangeId, interval)
                .map(ts -> new PremiumDetailLastBucketMeta(symbol, baseExchangeId, compareExchangeId, interval, ts));
    }
}
