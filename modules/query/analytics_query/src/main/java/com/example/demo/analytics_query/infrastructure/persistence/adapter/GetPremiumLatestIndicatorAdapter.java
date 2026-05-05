package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.analytics_query.application.dto.PremiumIndicatorView;
import com.example.demo.analytics_query.application.port.out.GetPremiumLatestIndicatorPort;
import com.example.demo.analytics_query.infrastructure.persistence.mapper.PremiumIndicatorViewMapper;
import com.example.demo.analytics_query.infrastructure.persistence.repo.PremiumIndicatorJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetPremiumLatestIndicatorAdapter implements GetPremiumLatestIndicatorPort {

    private final PremiumIndicatorJpaRepository repo;
    private final PremiumIndicatorViewMapper mapper;

    @Override
    public Optional<PremiumIndicatorView> findLatest(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, String type) {
        return repo.findLatest(symbol, baseExchangeId, compareExchangeId, interval, type, PageRequest.of(0, 1))
                .stream().findFirst().map(mapper::toView);
    }
}
