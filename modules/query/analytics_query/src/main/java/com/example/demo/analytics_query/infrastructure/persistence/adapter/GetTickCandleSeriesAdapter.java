package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.analytics_query.application.dto.TickCandleView;
import com.example.demo.analytics_query.application.port.out.GetTickCandleSeriesPort;
import com.example.demo.analytics_query.infrastructure.persistence.mapper.TickCandleViewMapper;
import com.example.demo.analytics_query.infrastructure.persistence.repo.TickCandleJpaRepository;
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
public class GetTickCandleSeriesAdapter implements GetTickCandleSeriesPort {

    private final TickCandleJpaRepository repo;
    private final TickCandleViewMapper mapper;

    @Override
    public List<TickCandleView> findSeries(Long marketCodeId, String interval, Long fromTs, Long toTs) {
        return repo.findByMarketCodeIdAndIntervalAndBucketOpenTsBetween(marketCodeId, interval, fromTs, toTs)
                .stream().map(mapper::toView).toList();
    }

    @Override
    public List<TickCandleView> findCursor(Long marketCodeId, String interval,
                                           Long cursor, int limit, CursorDirection direction) {
        Pageable pageable = PageRequest.of(0, limit);
        return switch (direction) {
            case BACKWARD -> {
                List<TickCandleView> desc = repo.findCursorBackward(marketCodeId, interval, cursor, pageable)
                        .stream().map(mapper::toView).collect(Collectors.toCollection(ArrayList::new));
                Collections.reverse(desc);
                yield desc;
            }
            case FORWARD -> repo.findCursorForward(marketCodeId, interval, cursor, pageable)
                    .stream().map(mapper::toView).toList();
        };
    }
}
