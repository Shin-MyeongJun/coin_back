package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.PremiumIndicatorView;
import com.example.demo.analytics_query.application.port.out.GetPremiumIndicatorSeriesPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPremiumIndicatorSeriesUseCase {

    private final GetPremiumIndicatorSeriesPort port;

    public List<PremiumIndicatorView> execute(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, String type, Long fromTs, Long toTs) {
        return port.findSeries(symbol, baseExchangeId, compareExchangeId, interval, type, fromTs, toTs);
    }
}
