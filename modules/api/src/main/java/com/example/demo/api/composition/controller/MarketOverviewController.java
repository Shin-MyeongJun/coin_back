package com.example.demo.api.composition.controller;

import com.example.demo.api.composition.dto.MarketOverviewResponse;
import com.example.demo.api.composition.service.MarketOverviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/compose")
@RequiredArgsConstructor
public class MarketOverviewController {

    private final MarketOverviewService marketOverviewService;

    @GetMapping("/market-overview/{marketCodeId}")
    public MarketOverviewResponse getMarketOverview(
            @PathVariable Long marketCodeId,
            @RequestParam String base,
            @RequestParam(required = false, defaultValue = "") String indicatorMarketCodeIds,
            @RequestParam(defaultValue = "1h") String interval,
            @RequestParam(defaultValue = "RSI") String indicatorType) {
        List<Long> ids = indicatorMarketCodeIds.isBlank()
                ? List.of(marketCodeId)
                : Arrays.stream(indicatorMarketCodeIds.split(",")).map(Long::parseLong).toList();
        return marketOverviewService.execute(marketCodeId, base, ids, interval, indicatorType);
    }
}
