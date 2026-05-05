package com.example.demo.market_data_query.infrastructure.persistence.adapter;

import com.example.demo.market_data_query.application.dto.PremiumDetailView;
import com.example.demo.market_data_query.application.port.out.GetPremiumDetailRawPort;
import com.example.demo.market_data_query.infrastructure.persistence.mapper.PremiumViewMapper;
import com.example.demo.market_data_query.infrastructure.persistence.repo.PremiumDetailJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

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
}
