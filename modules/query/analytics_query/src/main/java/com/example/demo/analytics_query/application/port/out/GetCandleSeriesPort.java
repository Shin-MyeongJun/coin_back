package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.CandleView;

import java.util.List;

public interface GetCandleSeriesPort {
    List<CandleView> findTickCandles(Long marketCodeId, String interval, Long fromTs, Long toTs);
    List<CandleView> findPremiumCandles(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, Long fromTs, Long toTs);
}
