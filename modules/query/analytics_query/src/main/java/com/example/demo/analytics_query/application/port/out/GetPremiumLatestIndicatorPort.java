package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.PremiumIndicatorView;

import java.util.Optional;

public interface GetPremiumLatestIndicatorPort {
    Optional<PremiumIndicatorView> findLatest(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, String type);
}
