package com.example.demo.api.composition.dto;

import com.example.demo.economic_query.application.dto.EconomicCalendarView;
import com.example.demo.market_data_query.application.dto.PremiumRankingView;
import com.example.demo.market_data_query.application.dto.TickBulkView;

import java.util.List;

public record DashboardResponse(
        List<TickBulkView> latestTicks,
        List<PremiumRankingView> premiumRanking,
        List<EconomicCalendarView> economicCalendar
) {
}
