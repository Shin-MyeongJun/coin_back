package com.example.demo.market_data_query.application.usecase;

import com.example.demo.market_data_query.application.dto.PremiumRankingView;
import com.example.demo.market_data_query.application.port.out.GetPremiumRankingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPremiumRankingUseCase {

    private final GetPremiumRankingPort port;

    public List<PremiumRankingView> execute(int limit) {
        return port.findTopN(limit);
    }
}
