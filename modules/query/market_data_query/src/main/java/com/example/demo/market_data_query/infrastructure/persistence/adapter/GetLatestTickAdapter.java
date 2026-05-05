package com.example.demo.market_data_query.infrastructure.persistence.adapter;

import com.example.demo.market_data_query.application.dto.TickLatestView;
import com.example.demo.market_data_query.application.port.out.GetLatestTickPort;
import com.example.demo.market_data_query.infrastructure.persistence.mapper.TickViewMapper;
import com.example.demo.market_data_query.infrastructure.persistence.repo.TickJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetLatestTickAdapter implements GetLatestTickPort {

    private final TickJpaRepository repo;
    private final TickViewMapper mapper;

    @Override
    public Optional<TickLatestView> findByMarketCodeId(Long marketCodeId) {
        return repo.findTop1ByMarketCodeIdOrderByTimestampDesc(marketCodeId)
                .map(mapper::toLatestView);
    }
}
