package com.example.demo.market_data_query.infrastructure.persistence.adapter;

import com.example.demo.infra_shard.paging.CursorDirection;
import com.example.demo.market_data_query.application.dto.FxView;
import com.example.demo.market_data_query.application.port.out.GetFxRawPort;
import com.example.demo.market_data_query.infrastructure.persistence.mapper.FxViewMapper;
import com.example.demo.market_data_query.infrastructure.persistence.repo.FxJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GetFxRawAdapter implements GetFxRawPort {

    private final FxJpaRepository repo;
    private final FxViewMapper mapper;

    @Override
    public List<FxView> findRaw(String baseCurrency, String quoteCurrency, Long fromTs, Long toTs) {
        return repo.findByBaseCurrencyAndQuoteCurrencyAndTimestampBetween(baseCurrency, quoteCurrency, fromTs, toTs)
                .stream().map(mapper::toView).toList();
    }

    @Override
    public List<FxView> findCursor(String baseCurrency, String quoteCurrency,
                                   Long cursor, int limit, CursorDirection direction) {
        Pageable pageable = PageRequest.of(0, limit);
        return switch (direction) {
            case BACKWARD -> {
                Long ceiling = (cursor == null) ? Long.MAX_VALUE : cursor;
                List<FxView> desc = repo
                        .findByBaseCurrencyAndQuoteCurrencyAndTimestampLessThanEqualOrderByTimestampDesc(
                                baseCurrency, quoteCurrency, ceiling, pageable)
                        .stream().map(mapper::toView).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
                Collections.reverse(desc);
                yield desc;
            }
            case FORWARD -> {
                Long floor = (cursor == null) ? Long.MIN_VALUE : cursor;
                yield repo
                        .findByBaseCurrencyAndQuoteCurrencyAndTimestampGreaterThanEqualOrderByTimestampAsc(
                                baseCurrency, quoteCurrency, floor, pageable)
                        .stream().map(mapper::toView).toList();
            }
        };
    }
}
