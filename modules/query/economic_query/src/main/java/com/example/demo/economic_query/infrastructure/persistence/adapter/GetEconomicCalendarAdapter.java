package com.example.demo.economic_query.infrastructure.persistence.adapter;

import com.example.demo.economic_query.application.dto.EconomicCalendarView;
import com.example.demo.economic_query.application.port.out.GetEconomicCalendarPort;
import com.example.demo.economic_query.infrastructure.persistence.mapper.IndicatorViewMapper;
import com.example.demo.economic_query.infrastructure.persistence.repo.EconomicScheduleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetEconomicCalendarAdapter implements GetEconomicCalendarPort {

    private final EconomicScheduleJpaRepository repository;
    private final IndicatorViewMapper mapper;

    @Override
    public List<EconomicCalendarView> findByReleaseDateBetween(Long from, Long to) {
        return repository.findByReleaseDateBetween(from, to).stream()
                .map(mapper::toCalendarView)
                .toList();
    }
}
