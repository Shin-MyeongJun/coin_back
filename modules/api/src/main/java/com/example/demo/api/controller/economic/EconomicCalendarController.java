package com.example.demo.api.controller.economic;

import com.example.demo.economic_query.application.dto.EconomicCalendarView;
import com.example.demo.economic_query.application.usecase.GetEconomicCalendarUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/economic")
@RequiredArgsConstructor
public class EconomicCalendarController {

    private final GetEconomicCalendarUseCase getEconomicCalendarUseCase;

    @GetMapping("/calendar")
    public List<EconomicCalendarView> getCalendar(
            @RequestParam Long fromTs,
            @RequestParam Long toTs) {
        return getEconomicCalendarUseCase.execute(fromTs, toTs);
    }
}
