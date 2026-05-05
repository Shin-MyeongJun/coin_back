package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.analytics_query.application.dto.PremiumDetailCandleView;
import com.example.demo.analytics_query.application.port.out.GetPremiumDetailCandleForRecomputePort;
import com.example.demo.analytics_query.infrastructure.persistence.mapper.PremiumDetailCandleViewMapper;
import com.example.demo.analytics_query.infrastructure.persistence.repo.PremiumDetailCandleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetPremiumDetailCandleForRecomputeAdapter implements GetPremiumDetailCandleForRecomputePort {

    private final PremiumDetailCandleJpaRepository repo;
    private final PremiumDetailCandleViewMapper mapper;

    @Override
    public List<PremiumDetailCandleView> findSeriesWithLookback(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, long fromTs, long toTs) {
        return repo.findSeries(symbol, baseExchangeId, compareExchangeId, interval, fromTs, toTs)
                .stream().map(mapper::toView).toList();
    }
}
