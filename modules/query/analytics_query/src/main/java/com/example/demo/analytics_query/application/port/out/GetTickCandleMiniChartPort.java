package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.TickCandleView;

import java.util.List;

public interface GetTickCandleMiniChartPort {
    List<TickCandleView> findTopN(Long marketCodeId, String interval, int limit);
}
