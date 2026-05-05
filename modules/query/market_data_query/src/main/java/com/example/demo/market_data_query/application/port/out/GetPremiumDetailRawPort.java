package com.example.demo.market_data_query.application.port.out;

import com.example.demo.market_data_query.application.dto.PremiumDetailView;

import java.util.List;

public interface GetPremiumDetailRawPort {
    List<PremiumDetailView> findRaw(Long baseExchangeId, Long compareExchangeId, String symbol, Long fromTs, Long toTs);
}
