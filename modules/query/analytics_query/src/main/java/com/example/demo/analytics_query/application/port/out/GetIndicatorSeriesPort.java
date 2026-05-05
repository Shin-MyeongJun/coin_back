package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.IndicatorView;

import java.util.List;

public interface GetIndicatorSeriesPort {
    List<IndicatorView> findTickIndicators(Long marketCodeId, String interval, String type, Long fromTs, Long toTs);
    List<IndicatorView> findPremiumIndicators(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, String type, Long fromTs, Long toTs);
}
