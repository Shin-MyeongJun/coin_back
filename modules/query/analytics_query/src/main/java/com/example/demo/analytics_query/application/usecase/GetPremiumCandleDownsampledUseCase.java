package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.PremiumCandleView;
import com.example.demo.analytics_query.application.port.out.GetPremiumCandleDownsampledPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPremiumCandleDownsampledUseCase {

    private final GetPremiumCandleDownsampledPort port;

    public List<PremiumCandleView> execute(String symbol, Long baseExchangeId, Long compareExchangeId, String sourceInterval, int targetBucketSeconds, Long fromTs, Long toTs) {
        return port.findDownsampled(symbol, baseExchangeId, compareExchangeId, sourceInterval, targetBucketSeconds, fromTs, toTs);
    }
}
