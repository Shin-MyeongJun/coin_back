package com.example.demo.economic_query.infrastructure.persistence.adapter;

import com.example.demo.economic_query.application.dto.IndicatorMetaView;
import com.example.demo.economic_query.application.port.out.GetIndicatorListByCategoryPort;
import com.example.demo.economic_query.infrastructure.persistence.mapper.IndicatorViewMapper;
import com.example.demo.economic_query.infrastructure.persistence.repo.EcoIndCodeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetIndicatorListByCategoryAdapter implements GetIndicatorListByCategoryPort {

    private final EcoIndCodeJpaRepository repository;
    private final IndicatorViewMapper mapper;

    @Override
    public List<IndicatorMetaView> findByType(String type) {
        return repository.findByType(type).stream()
                .map(mapper::toMetaView)
                .toList();
    }
}
