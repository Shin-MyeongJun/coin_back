package com.example.demo.economic_query.infrastructure.persistence.adapter;

import com.example.demo.economic_query.application.dto.CorrelationResultView;
import com.example.demo.economic_query.application.port.out.GetCorrelationResultPort;
import com.example.demo.economic_query.infrastructure.persistence.mapper.IndicatorViewMapper;
import com.example.demo.economic_query.infrastructure.persistence.repo.CorrelationResultJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetCorrelationResultAdapter implements GetCorrelationResultPort {

    private final CorrelationResultJpaRepository repository;
    private final IndicatorViewMapper mapper;

    @Override
    public List<CorrelationResultView> findByAssetSymbol(String assetSymbol) {
        return repository.findByAssetSymbol(assetSymbol).stream()
                .map(mapper::toCorrelationView)
                .toList();
    }
}
