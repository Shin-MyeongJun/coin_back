package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.IndicatorView;
import com.example.demo.analytics_query.application.port.out.GetLatestIndicatorPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetLatestIndicatorUseCase {

    private final GetLatestIndicatorPort port;

    public Optional<IndicatorView> executeForTick(Long marketCodeId, String interval, String type) {
        return port.findLatestTick(marketCodeId, interval, type);
    }

    public Optional<IndicatorView> executeForPremium(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, String type) {
        return port.findLatestPremium(symbol, baseExchangeId, compareExchangeId, interval, type);
    }
}
