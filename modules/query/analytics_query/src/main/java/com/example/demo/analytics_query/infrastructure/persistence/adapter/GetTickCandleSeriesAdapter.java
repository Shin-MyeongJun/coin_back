package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.analytics_query.application.dto.TickCandleView;
import com.example.demo.analytics_query.application.port.out.GetTickCandleSeriesPort;
import com.example.demo.analytics_query.infrastructure.persistence.mapper.TickCandleViewMapper;
import com.example.demo.analytics_query.infrastructure.persistence.repo.TickCandleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetTickCandleSeriesAdapter implements GetTickCandleSeriesPort {

    private final TickCandleJpaRepository repo;
    private final TickCandleViewMapper mapper;

    @Override
    public List<TickCandleView> findSeries(Long marketCodeId, String interval, Long fromTs, Long toTs) {
        return repo.findByMarketCodeIdAndIntervalAndBucketOpenTsBetween(marketCodeId, interval, fromTs, toTs)
                .stream().map(mapper::toView).toList();
    }
}
