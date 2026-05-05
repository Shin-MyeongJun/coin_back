package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.PremiumIndicatorView;

import java.util.List;

public interface GetPremiumIndicatorSeriesPort {
    List<PremiumIndicatorView> findSeries(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, String type, Long fromTs, Long toTs);
}
