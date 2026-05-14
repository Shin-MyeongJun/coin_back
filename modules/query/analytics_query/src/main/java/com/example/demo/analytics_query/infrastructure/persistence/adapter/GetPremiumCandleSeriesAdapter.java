package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.analytics_query.application.dto.PremiumCandleView;
import com.example.demo.analytics_query.application.port.out.GetPremiumCandleSeriesPort;
import com.example.demo.analytics_query.infrastructure.persistence.mapper.PremiumCandleViewMapper;
import com.example.demo.analytics_query.infrastructure.persistence.repo.PremiumCandleJpaRepository;
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
public class GetPremiumCandleSeriesAdapter implements GetPremiumCandleSeriesPort {

    private final PremiumCandleJpaRepository repo;
    private final PremiumCandleViewMapper mapper;

    @Override
    public List<PremiumCandleView> findSeries(String symbol, Long baseExchangeId, Long compareExchangeId,
                                              String interval, Long fromTs, Long toTs) {
        return repo.findSeries(symbol, baseExchangeId, compareExchangeId, interval, fromTs, toTs)
                .stream().map(mapper::toView).toList();
    }

    @Override
    public List<PremiumCandleView> findCursor(String symbol, Long baseExchangeId, Long compareExchangeId,
                                              String interval, Long cursor, int limit, CursorDirection direction) {
        Pageable pageable = PageRequest.of(0, limit);
        return switch (direction) {
            case BACKWARD -> {
                List<PremiumCandleView> desc = repo.findCursorBackward(
                                symbol, baseExchangeId, compareExchangeId, interval, cursor, pageable)
                        .stream().map(mapper::toView).collect(Collectors.toCollection(ArrayList::new));
                Collections.reverse(desc);
                yield desc;
            }
            case FORWARD -> repo.findCursorForward(
                            symbol, baseExchangeId, compareExchangeId, interval, cursor, pageable)
                    .stream().map(mapper::toView).toList();
        };
    }
}
