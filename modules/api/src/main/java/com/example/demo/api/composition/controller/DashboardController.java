package com.example.demo.api.composition.controller;

import com.example.demo.api.composition.dto.DashboardResponse;
import com.example.demo.api.composition.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/compose")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public DashboardResponse getDashboard(
            @RequestParam String marketCodeIds,
            @RequestParam(defaultValue = "10") int rankingLimit,
            @RequestParam Long calendarFromTs,
            @RequestParam Long calendarToTs) {
        List<Long> ids = Arrays.stream(marketCodeIds.split(","))
                .map(Long::parseLong)
                .toList();
        return dashboardService.execute(ids, rankingLimit, calendarFromTs, calendarToTs);
    }
}
