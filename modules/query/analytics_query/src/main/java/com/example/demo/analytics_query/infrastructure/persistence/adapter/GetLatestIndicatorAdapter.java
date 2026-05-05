package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.analytics_query.application.dto.IndicatorView;
import com.example.demo.analytics_query.application.port.out.GetLatestIndicatorPort;
import com.example.demo.analytics_query.infrastructure.persistence.mapper.IndicatorViewMapper;
import com.example.demo.analytics_query.infrastructure.persistence.repo.PremiumIndicatorJpaRepository;
import com.example.demo.analytics_query.infrastructure.persistence.repo.TickIndicatorJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetLatestIndicatorAdapter implements GetLatestIndicatorPort {

    private final TickIndicatorJpaRepository tickRepo;
    private final PremiumIndicatorJpaRepository premiumRepo;
    private final IndicatorViewMapper mapper;

    @Override
    public Optional<IndicatorView> findLatestTick(Long marketCodeId, String interval, String type) {
        return tickRepo.findTop1ByMarketCodeIdAndIntervalAndTypeOrderByBucketCloseTsDescIdDesc(marketCodeId, interval, type)
                .map(mapper::fromTickIndicator);
    }

    @Override
    public Optional<IndicatorView> findLatestPremium(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, String type) {
        return premiumRepo.findLatest(symbol, baseExchangeId, compareExchangeId, interval, type, PageRequest.of(0, 1))
                .stream().findFirst().map(mapper::fromPremiumIndicator);
    }
}
