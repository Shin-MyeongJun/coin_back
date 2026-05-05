package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.TickCandleView;

import java.util.List;

public interface GetTickCandleSeriesPort {
    List<TickCandleView> findSeries(Long marketCodeId, String interval, Long fromTs, Long toTs);
}
