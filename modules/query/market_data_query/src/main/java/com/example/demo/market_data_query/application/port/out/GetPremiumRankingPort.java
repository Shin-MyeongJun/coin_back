package com.example.demo.market_data_query.application.port.out;

import com.example.demo.market_data_query.application.dto.PremiumRankingView;

import java.util.List;

public interface GetPremiumRankingPort {
    List<PremiumRankingView> findTopN(int limit);
}
