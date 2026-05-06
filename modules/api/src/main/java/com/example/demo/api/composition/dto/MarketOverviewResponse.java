package com.example.demo.api.composition.dto;

import com.example.demo.analytics_query.application.dto.TickIndicatorView;
import com.example.demo.market_data_query.application.dto.PremiumSnapshotView;
import com.example.demo.market_data_query.application.dto.TickLatestView;

import java.util.List;

public record MarketOverviewResponse(
        TickLatestView latestTick,
        List<PremiumSnapshotView> premiumSnapshot,
        List<TickIndicatorView> latestIndicators
) {
}
