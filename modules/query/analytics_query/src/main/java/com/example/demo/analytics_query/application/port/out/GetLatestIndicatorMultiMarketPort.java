package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.IndicatorView;

import java.util.List;

public interface GetLatestIndicatorMultiMarketPort {
    List<IndicatorView> findLatestForMarkets(List<Long> marketCodeIds, String interval, String type);
}
