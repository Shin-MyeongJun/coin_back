package com.example.demo.api.controller.analytics;

import com.example.demo.analytics_query.application.dto.CandleView;
import com.example.demo.analytics_query.application.dto.LastBucketMeta;
import com.example.demo.analytics_query.application.usecase.GetLastClosedBucketUseCase;
import com.example.demo.analytics_query.application.usecase.GetPremiumCandleDownsampledUseCase;
import com.example.demo.analytics_query.application.usecase.GetPremiumCandleMiniChartUseCase;
import com.example.demo.analytics_query.application.usecase.GetPremiumCandleSeriesUseCase;
import com.example.demo.analytics_query.application.usecase.GetTickCandleDownsampledUseCase;
import com.example.demo.analytics_query.application.usecase.GetTickCandleMiniChartUseCase;
import com.example.demo.analytics_query.application.usecase.GetTickCandleSeriesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/analytics/candles")
@RequiredArgsConstructor
public class CandleController {

    private final GetTickCandleSeriesUseCase getTickCandleSeriesUseCase;
    private final GetPremiumCandleSeriesUseCase getPremiumCandleSeriesUseCase;
    private final GetTickCandleMiniChartUseCase getTickCandleMiniChartUseCase;
    private final GetPremiumCandleMiniChartUseCase getPremiumCandleMiniChartUseCase;
    private final GetTickCandleDownsampledUseCase getTickCandleDownsampledUseCase;
    private final GetPremiumCandleDownsampledUseCase getPremiumCandleDownsampledUseCase;
    private final GetLastClosedBucketUseCase getLastClosedBucketUseCase;

    @GetMapping
    public List<? extends CandleView> getSeries(
            @RequestParam String type,
            @RequestParam(required = false) Long marketCodeId,
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) Long baseExchangeId,
            @RequestParam(required = false) Long compareExchangeId,
            @RequestParam String interval,
            @RequestParam Long fromTs,
            @RequestParam Long toTs) {
        return switch (type) {
            case "tick" -> getTickCandleSeriesUseCase.execute(marketCodeId, interval, fromTs, toTs);
            case "premium" -> getPremiumCandleSeriesUseCase.execute(symbol, baseExchangeId, compareExchangeId, interval, fromTs, toTs);
            default -> throw new IllegalArgumentException("Unknown candle type: " + type);
        };
    }

    @GetMapping("/mini")
    public List<? extends CandleView> getMiniChart(
            @RequestParam String type,
            @RequestParam(required = false) Long marketCodeId,
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) Long baseExchangeId,
            @RequestParam(required = false) Long compareExchangeId,
            @RequestParam String interval,
            @RequestParam(defaultValue = "50") int limit) {
        return switch (type) {
            case "tick" -> getTickCandleMiniChartUseCase.execute(marketCodeId, interval, limit);
            case "premium" -> getPremiumCandleMiniChartUseCase.execute(symbol, baseExchangeId, compareExchangeId, interval, limit);
            default -> throw new IllegalArgumentException("Unknown candle type: " + type);
        };
    }

    @GetMapping("/downsampled")
    public List<? extends CandleView> getDownsampled(
            @RequestParam String type,
            @RequestParam(required = false) Long marketCodeId,
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) Long baseExchangeId,
            @RequestParam(required = false) Long compareExchangeId,
            @RequestParam String sourceInterval,
            @RequestParam int targetBucketSeconds,
            @RequestParam Long fromTs,
            @RequestParam Long toTs) {
        return switch (type) {
            case "tick" -> getTickCandleDownsampledUseCase.execute(marketCodeId, sourceInterval, targetBucketSeconds, fromTs, toTs);
            case "premium" -> getPremiumCandleDownsampledUseCase.execute(symbol, baseExchangeId, compareExchangeId, sourceInterval, targetBucketSeconds, fromTs, toTs);
            default -> throw new IllegalArgumentException("Unknown candle type: " + type);
        };
    }

    @GetMapping("/last-closed")
    public LastBucketMeta getLastClosed(
            @RequestParam String type,
            @RequestParam(required = false) Long marketCodeId,
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) Long baseExchangeId,
            @RequestParam(required = false) Long compareExchangeId,
            @RequestParam String interval) {
        return switch (type) {
            case "tick" -> getLastClosedBucketUseCase.executeForTick(marketCodeId, interval)
                    .orElseThrow(() -> new NoSuchElementException("No closed tick bucket found"));
            case "premium" -> getLastClosedBucketUseCase.executeForPremium(symbol, baseExchangeId, compareExchangeId, interval)
                    .orElseThrow(() -> new NoSuchElementException("No closed premium bucket found"));
            case "premium-detail" -> getLastClosedBucketUseCase.executeForPremiumDetail(symbol, baseExchangeId, compareExchangeId, interval)
                    .orElseThrow(() -> new NoSuchElementException("No closed premium-detail bucket found"));
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }
}
