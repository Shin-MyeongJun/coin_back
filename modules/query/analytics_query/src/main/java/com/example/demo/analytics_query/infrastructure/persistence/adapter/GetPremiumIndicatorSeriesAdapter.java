package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.analytics_query.application.dto.PremiumIndicatorView;
import com.example.demo.analytics_query.application.port.out.GetPremiumIndicatorSeriesPort;
import com.example.demo.analytics_query.infrastructure.persistence.mapper.PremiumIndicatorViewMapper;
import com.example.demo.analytics_query.infrastructure.persistence.repo.PremiumIndicatorJpaRepository;
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
public class GetPremiumIndicatorSeriesAdapter implements GetPremiumIndicatorSeriesPort {

    private final PremiumIndicatorJpaRepository repo;
    private final PremiumIndicatorViewMapper mapper;

    @Override
    public List<PremiumIndicatorView> findSeries(String symbol, Long baseExchangeId, Long compareExchangeId,
                                                 String interval, String type, Long fromTs, Long toTs) {
        return repo.findSeries(symbol, baseExchangeId, compareExchangeId, interval, type, fromTs, toTs)
                .stream().map(mapper::toView).toList();
    }

    @Override
    public List<PremiumIndicatorView> findCursor(String symbol, Long baseExchangeId, Long compareExchangeId,
                                                 String interval, String type,
                                                 Long cursor, int limit, CursorDirection direction) {
        Pageable pageable = PageRequest.of(0, limit);
        return switch (direction) {
            case BACKWARD -> {
                List<PremiumIndicatorView> desc = repo.findCursorBackward(
                                symbol, baseExchangeId, compareExchangeId, interval, type, cursor, pageable)
                        .stream().map(mapper::toView).collect(Collectors.toCollection(ArrayList::new));
                Collections.reverse(desc);
                yield desc;
            }
            case FORWARD -> repo.findCursorForward(
                            symbol, baseExchangeId, compareExchangeId, interval, type, cursor, pageable)
                    .stream().map(mapper::toView).toList();
        };
    }
}
