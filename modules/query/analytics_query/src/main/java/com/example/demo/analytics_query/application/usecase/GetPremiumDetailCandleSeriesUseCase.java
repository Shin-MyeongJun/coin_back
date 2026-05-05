package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.PremiumDetailCandleView;
import com.example.demo.analytics_query.application.port.out.GetPremiumDetailCandleSeriesPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPremiumDetailCandleSeriesUseCase {

    private final GetPremiumDetailCandleSeriesPort port;

    public List<PremiumDetailCandleView> execute(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, long fromTs, long toTs) {
        return port.findSeries(symbol, baseExchangeId, compareExchangeId, interval, fromTs, toTs);
    }
}
