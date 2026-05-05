package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.PremiumCandleView;

import java.util.List;

public interface GetPremiumCandleSeriesPort {
    List<PremiumCandleView> findSeries(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, Long fromTs, Long toTs);
}
