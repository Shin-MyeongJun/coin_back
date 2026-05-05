package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.analytics_query.application.dto.TickIndicatorView;
import com.example.demo.analytics_query.application.port.out.GetTickLatestIndicatorPort;
import com.example.demo.analytics_query.infrastructure.persistence.mapper.TickIndicatorViewMapper;
import com.example.demo.analytics_query.infrastructure.persistence.repo.TickIndicatorJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetTickLatestIndicatorAdapter implements GetTickLatestIndicatorPort {

    private final TickIndicatorJpaRepository repo;
    private final TickIndicatorViewMapper mapper;

    @Override
    public Optional<TickIndicatorView> findLatest(Long marketCodeId, String interval, String type) {
        return repo.findTop1ByMarketCodeIdAndIntervalAndTypeOrderByBucketCloseTsDescIdDesc(marketCodeId, interval, type)
                .map(mapper::toView);
    }
}
