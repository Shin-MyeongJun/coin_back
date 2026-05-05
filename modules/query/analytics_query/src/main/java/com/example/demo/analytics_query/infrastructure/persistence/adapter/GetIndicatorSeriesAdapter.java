package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.analytics_query.application.dto.IndicatorView;
import com.example.demo.analytics_query.application.port.out.GetIndicatorSeriesPort;
import com.example.demo.analytics_query.infrastructure.persistence.mapper.IndicatorViewMapper;
import com.example.demo.analytics_query.infrastructure.persistence.repo.PremiumIndicatorJpaRepository;
import com.example.demo.analytics_query.infrastructure.persistence.repo.TickIndicatorJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetIndicatorSeriesAdapter implements GetIndicatorSeriesPort {

    private final TickIndicatorJpaRepository tickRepo;
    private final PremiumIndicatorJpaRepository premiumRepo;
    private final IndicatorViewMapper mapper;

    @Override
    public List<IndicatorView> findTickIndicators(Long marketCodeId, String interval, String type, Long fromTs, Long toTs) {
        return tickRepo.findByMarketCodeIdAndIntervalAndTypeAndBucketOpenTsBetween(marketCodeId, interval, type, fromTs, toTs)
                .stream().map(mapper::fromTickIndicator).toList();
    }

    @Override
    public List<IndicatorView> findPremiumIndicators(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, String type, Long fromTs, Long toTs) {
        return premiumRepo.findSeries(symbol, baseExchangeId, compareExchangeId, interval, type, fromTs, toTs)
                .stream().map(mapper::fromPremiumIndicator).toList();
    }
}
