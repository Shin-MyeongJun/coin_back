package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.CandleView;
import com.example.demo.analytics_query.application.port.out.GetCandleMiniChartPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetCandleMiniChartUseCase {

    private final GetCandleMiniChartPort port;

    public List<CandleView> executeForTick(Long marketCodeId, String interval, int limit) {
        return port.findTopNTickCandles(marketCodeId, interval, limit);
    }

    public List<CandleView> executeForPremium(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, int limit) {
        return port.findTopNPremiumCandles(symbol, baseExchangeId, compareExchangeId, interval, limit);
    }
}
