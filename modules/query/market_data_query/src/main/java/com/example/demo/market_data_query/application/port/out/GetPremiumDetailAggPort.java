package com.example.demo.market_data_query.application.port.out;

import com.example.demo.market_data_query.application.dto.PremiumTimeSeriesView;

import java.util.List;

public interface GetPremiumDetailAggPort {
    List<PremiumTimeSeriesView> findAgg(Long baseExchangeId, Long compareExchangeId, String symbol, int bucketSeconds, Long fromTs, Long toTs);
}
