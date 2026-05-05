package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.PremiumCandleView;

import java.util.List;

public interface GetPremiumCandleDownsampledPort {
    List<PremiumCandleView> findDownsampled(String symbol, Long baseExchangeId, Long compareExchangeId, String sourceInterval, int targetBucketSeconds, Long fromTs, Long toTs);
}
