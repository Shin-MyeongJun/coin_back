package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.PremiumCandleView;
import com.example.demo.analytics_query.application.port.out.GetPremiumCandleMiniChartPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPremiumCandleMiniChartUseCase {

    private final GetPremiumCandleMiniChartPort port;

    public List<PremiumCandleView> execute(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, int limit) {
        return port.findTopN(symbol, baseExchangeId, compareExchangeId, interval, limit);
    }
}
