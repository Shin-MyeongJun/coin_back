package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.analytics_query.application.dto.PremiumCandleView;
import com.example.demo.analytics_query.application.port.out.GetPremiumCandleSeriesPort;
import com.example.demo.analytics_query.infrastructure.persistence.mapper.PremiumCandleViewMapper;
import com.example.demo.analytics_query.infrastructure.persistence.repo.PremiumCandleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetPremiumCandleSeriesAdapter implements GetPremiumCandleSeriesPort {

    private final PremiumCandleJpaRepository repo;
    private final PremiumCandleViewMapper mapper;

    @Override
    public List<PremiumCandleView> findSeries(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, Long fromTs, Long toTs) {
        return repo.findSeries(symbol, baseExchangeId, compareExchangeId, interval, fromTs, toTs)
                .stream().map(mapper::toView).toList();
    }
}
