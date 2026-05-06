package com.example.demo.api.composition.service;

import com.example.demo.analytics_query.application.usecase.GetTickCandleSeriesUseCase;
import com.example.demo.analytics_query.application.usecase.GetTickIndicatorSeriesUseCase;
import com.example.demo.api.composition.dto.ChartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChartCompositionService {

    private final GetTickCandleSeriesUseCase getTickCandleSeriesUseCase;
    private final GetTickIndicatorSeriesUseCase getTickIndicatorSeriesUseCase;

    public ChartResponse execute(Long marketCodeId, String interval, String indicatorType, Long fromTs, Long toTs) {
        var candles = getTickCandleSeriesUseCase.execute(marketCodeId, interval, fromTs, toTs);
        var indicators = getTickIndicatorSeriesUseCase.execute(marketCodeId, interval, indicatorType, fromTs, toTs);
        return new ChartResponse(candles, indicators);
    }
}
