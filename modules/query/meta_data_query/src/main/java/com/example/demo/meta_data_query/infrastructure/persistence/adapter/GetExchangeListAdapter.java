package com.example.demo.meta_data_query.infrastructure.persistence.adapter;

import com.example.demo.meta_data_query.application.dto.ExchangeView;
import com.example.demo.meta_data_query.application.port.out.GetExchangeListPort;
import com.example.demo.meta_data_query.infrastructure.persistence.mapper.ExchangeViewMapper;
import com.example.demo.meta_data_query.infrastructure.persistence.repo.ExchangeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetExchangeListAdapter implements GetExchangeListPort {

    private final ExchangeJpaRepository repository;
    private final ExchangeViewMapper mapper;

    @Override
    public List<ExchangeView> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
