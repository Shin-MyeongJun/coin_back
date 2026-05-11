package com.example.demo.meta_data_query.infrastructure.persistence.adapter;

import com.example.demo.meta_data_query.application.dto.MarketCodeView;
import com.example.demo.meta_data_query.application.port.out.GetMarketCodeListPort;
import com.example.demo.meta_data_query.infrastructure.persistence.mapper.MarketCodeViewMapper;
import com.example.demo.meta_data_query.infrastructure.persistence.repo.MarketCodeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetMarketCodeListAdapter implements GetMarketCodeListPort {

    private final MarketCodeJpaRepository repository;
    private final MarketCodeViewMapper mapper;

    @Override
    public List<MarketCodeView> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
