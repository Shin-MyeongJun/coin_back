package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.analytics_query.application.dto.CandleView;
import com.example.demo.analytics_query.application.port.out.GetCandleMiniChartPort;
import com.example.demo.analytics_query.infrastructure.persistence.mapper.CandleViewMapper;
import com.example.demo.analytics_query.infrastructure.persistence.repo.PremiumCandleJpaRepository;
import com.example.demo.analytics_query.infrastructure.persistence.repo.TickCandleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetCandleMiniChartAdapter implements GetCandleMiniChartPort {

    private final TickCandleJpaRepository tickRepo;
    private final PremiumCandleJpaRepository premiumRepo;
    private final CandleViewMapper mapper;

    @Override
    public List<CandleView> findTopNTickCandles(Long marketCodeId, String interval, int limit) {
        return tickRepo.findByMarketCodeIdAndIntervalOrderByBucketOpenTsDesc(marketCodeId, interval, PageRequest.of(0, limit))
                .stream().map(mapper::fromTickCandle).toList();
    }

    @Override
    public List<CandleView> findTopNPremiumCandles(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, int limit) {
        return premiumRepo.findMiniChart(symbol, baseExchangeId, compareExchangeId, interval, PageRequest.of(0, limit))
                .stream().map(mapper::fromPremiumCandle).toList();
    }
}
