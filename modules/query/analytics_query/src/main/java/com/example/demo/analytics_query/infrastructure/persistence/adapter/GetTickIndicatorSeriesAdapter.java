package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.analytics_query.application.dto.TickIndicatorView;
import com.example.demo.analytics_query.application.port.out.GetTickIndicatorSeriesPort;
import com.example.demo.analytics_query.infrastructure.persistence.mapper.TickIndicatorViewMapper;
import com.example.demo.analytics_query.infrastructure.persistence.repo.TickIndicatorJpaRepository;
import com.example.demo.infra_shard.paging.CursorDirection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetTickIndicatorSeriesAdapter implements GetTickIndicatorSeriesPort {

    private final TickIndicatorJpaRepository repo;
    private final TickIndicatorViewMapper mapper;

    @Override
    public List<TickIndicatorView> findSeries(Long marketCodeId, String interval, String type,
                                              Long fromTs, Long toTs) {
        return repo.findByMarketCodeIdAndIntervalAndTypeAndBucketOpenTsBetween(
                        marketCodeId, interval, type, fromTs, toTs)
                .stream().map(mapper::toView).toList();
    }

    @Override
    public List<TickIndicatorView> findCursor(Long marketCodeId, String interval, String type,
                                              Long cursor, int limit, CursorDirection direction) {
        Pageable pageable = PageRequest.of(0, limit);
        return switch (direction) {
            case BACKWARD -> {
                List<TickIndicatorView> desc = repo.findCursorBackward(
                                marketCodeId, interval, type, cursor, pageable)
                        .stream().map(mapper::toView).collect(Collectors.toCollection(ArrayList::new));
                Collections.reverse(desc);
                yield desc;
            }
            case FORWARD -> repo.findCursorForward(marketCodeId, interval, type, cursor, pageable)
                    .stream().map(mapper::toView).toList();
        };
    }
}
