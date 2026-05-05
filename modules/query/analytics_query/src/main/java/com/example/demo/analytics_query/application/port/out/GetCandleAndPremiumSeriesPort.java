package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.CandleAndPremiumView;

import java.util.List;

public interface GetCandleAndPremiumSeriesPort {
    List<CandleAndPremiumView> find(Long marketCodeId, Long baseExchangeId, Long compareExchangeId, String interval, Long fromTs, Long toTs);
}
