package com.example.demo.economic_query.infrastructure.persistence.adapter;

import com.example.demo.economic_query.application.dto.IndicatorMetaView;
import com.example.demo.economic_query.application.port.out.GetIndicatorMetaPort;
import com.example.demo.economic_query.infrastructure.persistence.mapper.IndicatorViewMapper;
import com.example.demo.economic_query.infrastructure.persistence.repo.EcoIndCodeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetIndicatorMetaAdapter implements GetIndicatorMetaPort {

    private final EcoIndCodeJpaRepository repository;
    private final IndicatorViewMapper mapper;

    @Override
    public Optional<IndicatorMetaView> findById(Long id) {
        return repository.findById(id).map(mapper::toMetaView);
    }

    @Override
    public List<IndicatorMetaView> findAll() {
        return repository.findAll().stream()
                .map(mapper::toMetaView)
                .toList();
    }
}
