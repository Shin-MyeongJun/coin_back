package com.example.demo.economic_query.infrastructure.persistence.adapter;

import com.example.demo.economic_query.application.dto.IndicatorSeriesView;
import com.example.demo.economic_query.application.port.out.GetIndicatorSeriesPort;
import com.example.demo.economic_query.infrastructure.persistence.mapper.IndicatorViewMapper;
import com.example.demo.economic_query.infrastructure.persistence.repo.EcoIndJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetIndicatorSeriesAdapter implements GetIndicatorSeriesPort {

    private final EcoIndJpaRepository repository;
    private final IndicatorViewMapper mapper;

    @Override
    public List<IndicatorSeriesView> findByIndCodeIdAndDateRange(Long indCodeId, Long from, Long to) {
        return repository.findByIndCodeIdAndObservationDateBetween(indCodeId, from, to).stream()
                .map(mapper::toSeriesView)
                .toList();
    }
}
