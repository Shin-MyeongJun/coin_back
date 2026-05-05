package com.example.demo.market_data_query.application.usecase;

import com.example.demo.market_data_query.application.dto.PremiumDetailView;
import com.example.demo.market_data_query.application.port.out.GetPremiumDetailRawPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPremiumDetailRawUseCase {

    private final GetPremiumDetailRawPort port;

    public List<PremiumDetailView> execute(Long baseExchangeId, Long compareExchangeId, String symbol,
                                           Long fromTs, Long toTs) {
        return port.findRaw(baseExchangeId, compareExchangeId, symbol, fromTs, toTs);
    }
}
