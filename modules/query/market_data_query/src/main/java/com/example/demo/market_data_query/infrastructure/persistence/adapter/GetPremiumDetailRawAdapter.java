package com.example.demo.market_data_query.infrastructure.persistence.adapter;

import com.example.demo.infra_shard.paging.CursorDirection;
import com.example.demo.market_data_query.application.dto.PremiumDetailView;
import com.example.demo.market_data_query.application.port.out.GetPremiumDetailRawPort;
import com.example.demo.market_data_query.infrastructure.persistence.mapper.PremiumViewMapper;
import com.example.demo.market_data_query.infrastructure.persistence.repo.PremiumDetailJpaRepository;
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
public class GetPremiumDetailRawAdapter implements GetPremiumDetailRawPort {

    private final PremiumDetailJpaRepository repo;
    private final PremiumViewMapper mapper;

    @Override
    public List<PremiumDetailView> findRaw(Long baseExchangeId, Long compareExchangeId,
                                           String symbol, Long fromTs, Long toTs) {
        return repo.findByBaseExchangeIdAndCompareExchangeIdAndSymbolAndTimestampBetween(
                        baseExchangeId, compareExchangeId, symbol, fromTs, toTs)
                .stream().map(mapper::toDetailView).toList();
    }

    @Override
    public List<PremiumDetailView> findCursor(Long baseExchangeId, Long compareExchangeId, String symbol,
                                              Long cursor, int limit, CursorDirection direction) {
        Pageable pageable = PageRequest.of(0, limit);
        return switch (direction) {
            case BACKWARD -> {
                Long ceiling = (cursor == null) ? Long.MAX_VALUE : cursor;
                List<PremiumDetailView> desc = repo
                        .findByBaseExchangeIdAndCompareExchangeIdAndSymbolAndTimestampLessThanEqualOrderByTimestampDesc(
                                baseExchangeId, compareExchangeId, symbol, ceiling, pageable)
                        .stream().map(mapper::toDetailView).collect(Collectors.toCollection(ArrayList::new));
                Collections.reverse(desc);
                yield desc;
            }
            case FORWARD -> {
                Long floor = (cursor == null) ? Long.MIN_VALUE : cursor;
                yield repo
                        .findByBaseExchangeIdAndCompareExchangeIdAndSymbolAndTimestampGreaterThanEqualOrderByTimestampAsc(
                                baseExchangeId, compareExchangeId, symbol, floor, pageable)
                        .stream().map(mapper::toDetailView).toList();
            }
        };
    }
}
