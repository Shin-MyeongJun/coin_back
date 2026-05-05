package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.CandleView;

import java.util.List;

public interface GetCandleMiniChartPort {
    List<CandleView> findTopNTickCandles(Long marketCodeId, String interval, int limit);
    List<CandleView> findTopNPremiumCandles(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, int limit);
}
