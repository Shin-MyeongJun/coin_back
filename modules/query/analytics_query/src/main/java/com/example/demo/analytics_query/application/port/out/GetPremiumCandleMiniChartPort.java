package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.PremiumCandleView;

import java.util.List;

public interface GetPremiumCandleMiniChartPort {
    List<PremiumCandleView> findTopN(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, int limit);
}
