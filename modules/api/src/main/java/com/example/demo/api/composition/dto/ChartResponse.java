package com.example.demo.api.composition.dto;

import com.example.demo.analytics_query.application.dto.TickCandleView;
import com.example.demo.analytics_query.application.dto.TickIndicatorView;

import java.util.List;

public record ChartResponse(
        List<TickCandleView> candles,
        List<TickIndicatorView> indicators
) {
}
