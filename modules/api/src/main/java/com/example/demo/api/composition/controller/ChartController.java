package com.example.demo.api.composition.controller;

import com.example.demo.api.composition.dto.ChartResponse;
import com.example.demo.api.composition.service.ChartCompositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/compose")
@RequiredArgsConstructor
public class ChartController {

    private final ChartCompositionService chartCompositionService;

    @GetMapping("/chart/{marketCodeId}")
    public ChartResponse getChart(
            @PathVariable Long marketCodeId,
            @RequestParam String interval,
            @RequestParam(defaultValue = "RSI") String indicatorType,
            @RequestParam Long fromTs,
            @RequestParam Long toTs) {
        return chartCompositionService.execute(marketCodeId, interval, indicatorType, fromTs, toTs);
    }
}
