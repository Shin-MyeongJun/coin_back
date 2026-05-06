package com.example.demo.api.composition.service;

import com.example.demo.analytics_query.application.usecase.GetLatestIndicatorMultiMarketUseCase;
import com.example.demo.api.composition.dto.MarketOverviewResponse;
import com.example.demo.market_data_query.application.usecase.GetLatestTickUseCase;
import com.example.demo.market_data_query.application.usecase.GetPremiumSnapshotByBaseUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class MarketOverviewService {

    private final GetLatestTickUseCase getLatestTickUseCase;
    private final GetPremiumSnapshotByBaseUseCase getPremiumSnapshotByBaseUseCase;
    private final GetLatestIndicatorMultiMarketUseCase getLatestIndicatorMultiMarketUseCase;

    public MarketOverviewResponse execute(Long marketCodeId, String base, List<Long> indicatorMarketCodeIds, String interval, String indicatorType) {
        var tick = getLatestTickUseCase.execute(marketCodeId)
                .orElseThrow(() -> new NoSuchElementException("No tick found for marketCodeId: " + marketCodeId));
        var premium = getPremiumSnapshotByBaseUseCase.execute(base);
        var indicators = getLatestIndicatorMultiMarketUseCase.execute(indicatorMarketCodeIds, interval, indicatorType);
        return new MarketOverviewResponse(tick, premium, indicators);
    }
}
