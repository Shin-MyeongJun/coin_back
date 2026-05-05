package com.example.demo.economic_query.application.port.out;

import com.example.demo.economic_query.application.dto.IndicatorSeriesView;

import java.util.List;

public interface GetIndicatorSeriesPort {
    List<IndicatorSeriesView> findByIndCodeIdAndDateRange(Long indCodeId, Long from, Long to);
}
