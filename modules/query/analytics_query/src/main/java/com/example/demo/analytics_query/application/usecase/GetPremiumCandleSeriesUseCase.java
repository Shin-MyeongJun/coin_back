package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.PremiumCandleView;
import com.example.demo.analytics_query.application.port.out.GetPremiumCandleSeriesPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPremiumCandleSeriesUseCase {

    private final GetPremiumCandleSeriesPort port;

    public List<PremiumCandleView> execute(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, Long fromTs, Long toTs) {
        return port.findSeries(symbol, baseExchangeId, compareExchangeId, interval, fromTs, toTs);
    }
}
