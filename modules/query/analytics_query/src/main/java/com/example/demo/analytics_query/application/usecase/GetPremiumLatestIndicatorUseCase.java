package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.PremiumIndicatorView;
import com.example.demo.analytics_query.application.port.out.GetPremiumLatestIndicatorPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetPremiumLatestIndicatorUseCase {

    private final GetPremiumLatestIndicatorPort port;

    public Optional<PremiumIndicatorView> execute(String symbol, Long baseExchangeId, Long compareExchangeId, String interval, String type) {
        return port.findLatest(symbol, baseExchangeId, compareExchangeId, interval, type);
    }
}
