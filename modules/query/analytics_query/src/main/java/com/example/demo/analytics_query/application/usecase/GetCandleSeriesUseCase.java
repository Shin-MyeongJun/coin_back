package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.CandleView;
import com.example.demo.analytics_query.application.port.out.GetCandleSeriesPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetCandleSeriesUseCase {

    private final GetCandleSeriesPort port;

    public List<CandleView> executeForTick(Long marketCodeId, String interval, Long fromTs, Long toTs) {
        return port.findTickCandles(marketCodeId, interval, fromTs, toTs);
    }

    public List<CandleView> executeForPremium(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, Long fromTs, Long toTs) {
        return port.findPremiumCandles(symbol, baseExchangeId, compareExchangeId, interval, fromTs, toTs);
    }
}
