package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.analytics_query.application.dto.PremiumIndicatorView;
import com.example.demo.analytics_query.application.port.out.GetPremiumIndicatorSeriesPort;
import com.example.demo.analytics_query.infrastructure.persistence.mapper.PremiumIndicatorViewMapper;
import com.example.demo.analytics_query.infrastructure.persistence.repo.PremiumIndicatorJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetPremiumIndicatorSeriesAdapter implements GetPremiumIndicatorSeriesPort {

    private final PremiumIndicatorJpaRepository repo;
    private final PremiumIndicatorViewMapper mapper;

    @Override
    public List<PremiumIndicatorView> findSeries(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, String type, Long fromTs, Long toTs) {
        return repo.findSeries(symbol, baseExchangeId, compareExchangeId, interval, type, fromTs, toTs)
                .stream().map(mapper::toView).toList();
    }
}
