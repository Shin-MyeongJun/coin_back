package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.PremiumDetailCandleView;

import java.util.List;

public interface GetPremiumDetailCandleForRecomputePort {
    List<PremiumDetailCandleView> findSeriesWithLookback(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, long fromTs, long toTs);
}
