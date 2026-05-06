package com.example.demo.api.controller.analytics;

import com.example.demo.analytics_query.application.dto.ScreenerCondition;
import com.example.demo.analytics_query.application.dto.ScreenerResult;
import com.example.demo.analytics_query.application.usecase.GetScreenerUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics/screener")
@RequiredArgsConstructor
public class ScreenerController {

    private final GetScreenerUseCase getScreenerUseCase;

    @GetMapping
    public List<? extends ScreenerResult> screen(
            @RequestParam String type,
            @RequestParam String interval,
            @RequestParam String indicatorType,
            @RequestParam(required = false) Integer period,
            @RequestParam(required = false) BigDecimal minValue,
            @RequestParam(required = false) BigDecimal maxValue) {
        List<ScreenerCondition> conditions = List.of(
                new ScreenerCondition(interval, indicatorType, period, minValue, maxValue));
        return switch (type) {
            case "tick" -> getScreenerUseCase.executeForTick(conditions);
            case "premium" -> getScreenerUseCase.executeForPremium(conditions);
            default -> throw new IllegalArgumentException("Unknown screener type: " + type);
        };
    }
}
