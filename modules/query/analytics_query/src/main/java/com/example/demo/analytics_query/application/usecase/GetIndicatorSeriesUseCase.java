package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.IndicatorView;
import com.example.demo.analytics_query.application.port.out.GetIndicatorSeriesPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetIndicatorSeriesUseCase {

    private final GetIndicatorSeriesPort port;

    public List<IndicatorView> executeForTick(Long marketCodeId, String interval, String type, Long fromTs, Long toTs) {
        return port.findTickIndicators(marketCodeId, interval, type, fromTs, toTs);
    }

    public List<IndicatorView> executeForPremium(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, String type, Long fromTs, Long toTs) {
        return port.findPremiumIndicators(symbol, baseExchangeId, compareExchangeId, interval, type, fromTs, toTs);
    }
}
