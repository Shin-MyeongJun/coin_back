package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.PremiumDetailLastBucketMeta;
import com.example.demo.analytics_query.application.dto.PremiumLastBucketMeta;
import com.example.demo.analytics_query.application.dto.TickLastBucketMeta;
import com.example.demo.analytics_query.application.port.out.GetLastClosedBucketPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetLastClosedBucketUseCase {

    private final GetLastClosedBucketPort port;

    public Optional<TickLastBucketMeta> executeForTick(Long marketCodeId, String interval) {
        return port.findLastTick(marketCodeId, interval);
    }

    public Optional<PremiumLastBucketMeta> executeForPremium(String symbol, Long baseExchangeId, Long compareExchangeId, String interval) {
        return port.findLastPremium(symbol, baseExchangeId, compareExchangeId, interval);
    }

    public Optional<PremiumDetailLastBucketMeta> executeForPremiumDetail(String symbol, Long baseExchangeId, Long compareExchangeId, String interval) {
        return port.findLastPremiumDetail(symbol, baseExchangeId, compareExchangeId, interval);
    }
}
