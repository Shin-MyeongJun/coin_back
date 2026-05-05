package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.analytics_query.application.dto.CandleView;
import com.example.demo.analytics_query.application.port.out.GetCandleSeriesPort;
import com.example.demo.analytics_query.infrastructure.persistence.mapper.CandleViewMapper;
import com.example.demo.analytics_query.infrastructure.persistence.repo.PremiumCandleJpaRepository;
import com.example.demo.analytics_query.infrastructure.persistence.repo.TickCandleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetCandleSeriesAdapter implements GetCandleSeriesPort {

    private final TickCandleJpaRepository tickRepo;
    private final PremiumCandleJpaRepository premiumRepo;
    private final CandleViewMapper mapper;

    @Override
    public List<CandleView> findTickCandles(Long marketCodeId, String interval, Long fromTs, Long toTs) {
        return tickRepo.findByMarketCodeIdAndIntervalAndBucketOpenTsBetween(marketCodeId, interval, fromTs, toTs)
                .stream().map(mapper::fromTickCandle).toList();
    }

    @Override
    public List<CandleView> findPremiumCandles(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, Long fromTs, Long toTs) {
        return premiumRepo.findSeries(symbol, baseExchangeId, compareExchangeId, interval, fromTs, toTs)
                .stream().map(mapper::fromPremiumCandle).toList();
    }
}
