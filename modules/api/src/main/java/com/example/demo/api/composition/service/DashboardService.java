package com.example.demo.api.composition.service;

import com.example.demo.api.composition.dto.DashboardResponse;
import com.example.demo.economic_query.application.usecase.GetEconomicCalendarUseCase;
import com.example.demo.market_data_query.application.usecase.GetLatestTickBulkUseCase;
import com.example.demo.market_data_query.application.usecase.GetPremiumRankingUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final GetLatestTickBulkUseCase getLatestTickBulkUseCase;
    private final GetPremiumRankingUseCase getPremiumRankingUseCase;
    private final GetEconomicCalendarUseCase getEconomicCalendarUseCase;

    public DashboardResponse execute(List<Long> marketCodeIds, int rankingLimit, Long calendarFromTs, Long calendarToTs) {
        var ticks = getLatestTickBulkUseCase.execute(marketCodeIds);
        var ranking = getPremiumRankingUseCase.execute(rankingLimit);
        var calendar = getEconomicCalendarUseCase.execute(calendarFromTs, calendarToTs);
        return new DashboardResponse(ticks, ranking, calendar);
    }
}
