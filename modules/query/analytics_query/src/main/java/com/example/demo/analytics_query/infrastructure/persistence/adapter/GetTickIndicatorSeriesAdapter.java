package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.analytics_query.application.dto.TickIndicatorView;
import com.example.demo.analytics_query.application.port.out.GetTickIndicatorSeriesPort;
import com.example.demo.analytics_query.infrastructure.persistence.mapper.TickIndicatorViewMapper;
import com.example.demo.analytics_query.infrastructure.persistence.repo.TickIndicatorJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetTickIndicatorSeriesAdapter implements GetTickIndicatorSeriesPort {

    private final TickIndicatorJpaRepository repo;
    private final TickIndicatorViewMapper mapper;

    @Override
    public List<TickIndicatorView> findSeries(Long marketCodeId, String interval, String type, Long fromTs, Long toTs) {
        return repo.findByMarketCodeIdAndIntervalAndTypeAndBucketOpenTsBetween(marketCodeId, interval, type, fromTs, toTs)
                .stream().map(mapper::toView).toList();
    }
}
