package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.analytics_query.application.dto.TickCandleView;
import com.example.demo.analytics_query.application.port.out.GetTickCandleMiniChartPort;
import com.example.demo.analytics_query.infrastructure.persistence.mapper.TickCandleViewMapper;
import com.example.demo.analytics_query.infrastructure.persistence.repo.TickCandleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetTickCandleMiniChartAdapter implements GetTickCandleMiniChartPort {

    private final TickCandleJpaRepository repo;
    private final TickCandleViewMapper mapper;

    @Override
    public List<TickCandleView> findTopN(Long marketCodeId, String interval, int limit) {
        return repo.findByMarketCodeIdAndIntervalOrderByBucketOpenTsDesc(marketCodeId, interval, PageRequest.of(0, limit))
                .stream().map(mapper::toView).toList();
    }
}
