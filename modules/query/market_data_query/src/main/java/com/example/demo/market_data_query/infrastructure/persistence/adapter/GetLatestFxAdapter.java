package com.example.demo.market_data_query.infrastructure.persistence.adapter;

import com.example.demo.market_data_query.application.dto.FxView;
import com.example.demo.market_data_query.application.port.out.GetLatestFxPort;
import com.example.demo.market_data_query.infrastructure.persistence.mapper.FxViewMapper;
import com.example.demo.market_data_query.infrastructure.persistence.repo.FxJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetLatestFxAdapter implements GetLatestFxPort {

    private final FxJpaRepository repo;
    private final FxViewMapper mapper;

    @Override
    public Optional<FxView> findLatest(String baseCurrency, String quoteCurrency) {
        return repo.findTop1ByBaseCurrencyAndQuoteCurrencyOrderByTimestampDesc(baseCurrency, quoteCurrency)
                .map(mapper::toView);
    }
}
