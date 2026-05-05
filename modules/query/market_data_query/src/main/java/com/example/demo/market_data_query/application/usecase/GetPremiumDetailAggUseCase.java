package com.example.demo.market_data_query.application.usecase;

import com.example.demo.market_data_query.application.dto.PremiumTimeSeriesView;
import com.example.demo.market_data_query.application.port.out.GetPremiumDetailAggPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPremiumDetailAggUseCase {

    private final GetPremiumDetailAggPort port;

    public List<PremiumTimeSeriesView> execute(Long baseExchangeId, Long compareExchangeId, String symbol,
                                               int bucketSeconds, Long fromTs, Long toTs) {
        return port.findAgg(baseExchangeId, compareExchangeId, symbol, bucketSeconds, fromTs, toTs);
    }
}
