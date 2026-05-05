package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.TickIndicatorView;

import java.util.List;

public interface GetTickIndicatorSeriesPort {
    List<TickIndicatorView> findSeries(Long marketCodeId, String interval, String type, Long fromTs, Long toTs);
}
