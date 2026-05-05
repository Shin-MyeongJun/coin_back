package com.example.demo.economic_query.application.port.out;

import com.example.demo.economic_query.application.dto.EconomicCalendarView;

import java.util.List;

public interface GetEconomicCalendarPort {
    List<EconomicCalendarView> findByReleaseDateBetween(Long from, Long to);
}
