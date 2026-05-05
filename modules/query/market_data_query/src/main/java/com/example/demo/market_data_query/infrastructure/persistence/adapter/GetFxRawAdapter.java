package com.example.demo.market_data_query.infrastructure.persistence.adapter;

import com.example.demo.market_data_query.application.dto.FxView;
import com.example.demo.market_data_query.application.port.out.GetFxRawPort;
import com.example.demo.market_data_query.infrastructure.persistence.mapper.FxViewMapper;
import com.example.demo.market_data_query.infrastructure.persistence.repo.FxJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
}
