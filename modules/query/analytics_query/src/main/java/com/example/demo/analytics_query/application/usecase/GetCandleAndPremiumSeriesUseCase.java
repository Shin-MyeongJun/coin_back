package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.CandleAndPremiumView;
import com.example.demo.analytics_query.application.port.out.GetCandleAndPremiumSeriesPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetCandleAndPremiumSeriesUseCase {

    private final GetCandleAndPremiumSeriesPort port;

    public List<CandleAndPremiumView> execute(Long marketCodeId, Long baseExchangeId, Long compareExchangeId, String interval, Long fromTs, Long toTs) {
        return port.find(marketCodeId, baseExchangeId, compareExchangeId, interval, fromTs, toTs);
    }
}
