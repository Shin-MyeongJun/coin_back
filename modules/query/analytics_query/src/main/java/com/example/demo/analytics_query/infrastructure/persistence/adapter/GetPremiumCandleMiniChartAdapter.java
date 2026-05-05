package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.analytics_query.application.dto.PremiumCandleView;
import com.example.demo.analytics_query.application.port.out.GetPremiumCandleMiniChartPort;
import com.example.demo.analytics_query.infrastructure.persistence.mapper.PremiumCandleViewMapper;
import com.example.demo.analytics_query.infrastructure.persistence.repo.PremiumCandleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetPremiumCandleMiniChartAdapter implements GetPremiumCandleMiniChartPort {

    private final PremiumCandleJpaRepository repo;
    private final PremiumCandleViewMapper mapper;

    @Override
    public List<PremiumCandleView> findTopN(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, int limit) {
        return repo.findMiniChart(symbol, baseExchangeId, compareExchangeId, interval, PageRequest.of(0, limit))
                .stream().map(mapper::toView).toList();
    }
}
