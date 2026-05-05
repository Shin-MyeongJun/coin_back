package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.IndicatorView;

import java.util.Optional;

public interface GetLatestIndicatorPort {
    Optional<IndicatorView> findLatestTick(Long marketCodeId, String interval, String type);
    Optional<IndicatorView> findLatestPremium(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, String type);
}
