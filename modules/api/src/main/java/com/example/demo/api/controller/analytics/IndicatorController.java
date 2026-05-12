package com.example.demo.api.controller.analytics;

import com.example.demo.analytics_query.application.dto.IndicatorView;
import com.example.demo.analytics_query.application.dto.TickIndicatorView;
import com.example.demo.analytics_query.application.usecase.GetLatestIndicatorMultiMarketUseCase;
import com.example.demo.analytics_query.application.usecase.GetPremiumIndicatorSeriesUseCase;
import com.example.demo.analytics_query.application.usecase.GetPremiumLatestIndicatorUseCase;
import com.example.demo.analytics_query.application.usecase.GetTickIndicatorSeriesUseCase;
import com.example.demo.analytics_query.application.usecase.GetTickLatestIndicatorUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

@RestController("analyticsIndicatorController")
@RequestMapping("/api/v1/analytics/indicators")
@RequiredArgsConstructor
public class IndicatorController {

    private final GetTickIndicatorSeriesUseCase getTickIndicatorSeriesUseCase;
    private final GetPremiumIndicatorSeriesUseCase getPremiumIndicatorSeriesUseCase;
    private final GetTickLatestIndicatorUseCase getTickLatestIndicatorUseCase;
    private final GetPremiumLatestIndicatorUseCase getPremiumLatestIndicatorUseCase;
    private final GetLatestIndicatorMultiMarketUseCase getLatestIndicatorMultiMarketUseCase;

    @GetMapping
    public List<? extends IndicatorView> getSeries(
            @RequestParam String type,
            @RequestParam(required = false) Long marketCodeId,
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) Long baseExchangeId,
            @RequestParam(required = false) Long compareExchangeId,
            @RequestParam String interval,
            @RequestParam String indicatorType,
            @RequestParam Long fromTs,
            @RequestParam Long toTs) {
        return switch (type) {
            case "tick" -> getTickIndicatorSeriesUseCase.execute(marketCodeId, interval, indicatorType, fromTs, toTs);
            case "premium" -> getPremiumIndicatorSeriesUseCase.execute(symbol, baseExchangeId, compareExchangeId, interval, indicatorType, fromTs, toTs);
            default -> throw new IllegalArgumentException("Unknown indicator type: " + type);
        };
    }

    @GetMapping("/latest")
    public IndicatorView getLatest(
            @RequestParam String type,
            @RequestParam(required = false) Long marketCodeId,
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) Long baseExchangeId,
            @RequestParam(required = false) Long compareExchangeId,
            @RequestParam String interval,
            @RequestParam String indicatorType) {
        return switch (type) {
            case "tick" -> getTickLatestIndicatorUseCase.execute(marketCodeId, interval, indicatorType)
                    .orElseThrow(() -> new NoSuchElementException("No indicator found"));
            case "premium" -> getPremiumLatestIndicatorUseCase.execute(symbol, baseExchangeId, compareExchangeId, interval, indicatorType)
                    .orElseThrow(() -> new NoSuchElementException("No indicator found"));
            default -> throw new IllegalArgumentException("Unknown indicator type: " + type);
        };
    }

    @GetMapping("/latest/multi")
    public List<TickIndicatorView> getLatestMulti(
            @RequestParam String marketCodeIds,
            @RequestParam String interval,
            @RequestParam String indicatorType) {
        List<Long> ids = Arrays.stream(marketCodeIds.split(","))
                .map(Long::parseLong)
                .toList();
        return getLatestIndicatorMultiMarketUseCase.execute(ids, interval, indicatorType);
    }
}
