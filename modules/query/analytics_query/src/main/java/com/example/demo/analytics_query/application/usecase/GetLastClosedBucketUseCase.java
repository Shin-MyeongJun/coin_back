package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.LastBucketMeta;
import com.example.demo.analytics_query.application.port.out.GetLastClosedBucketPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetLastClosedBucketUseCase {

    private final GetLastClosedBucketPort port;

    public Optional<LastBucketMeta> executeForTick(Long marketCodeId, String interval) {
        return port.findLastTickCandle(marketCodeId, interval);
    }

    public Optional<LastBucketMeta> executeForPremium(String symbol, Long baseExchangeId, Long compareExchangeId, String interval) {
        return port.findLastPremiumCandle(symbol, baseExchangeId, compareExchangeId, interval);
    }
}
