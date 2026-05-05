package com.example.demo.economic_query.application.usecase;

import com.example.demo.economic_query.application.dto.EconomicCalendarView;
import com.example.demo.economic_query.application.port.out.GetEconomicCalendarPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetEconomicCalendarUseCase {

    private final GetEconomicCalendarPort port;

    public List<EconomicCalendarView> execute(Long from, Long to) {
        return port.findByReleaseDateBetween(from, to);
    }
}
