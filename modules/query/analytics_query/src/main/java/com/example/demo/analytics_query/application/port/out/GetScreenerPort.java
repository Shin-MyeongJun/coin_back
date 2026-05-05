package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.ScreenerResult;

import java.math.BigDecimal;
import java.util.List;

public interface GetScreenerPort {
    List<ScreenerResult> findByIndicatorCondition(String interval, String type, BigDecimal minValue, BigDecimal maxValue);
}
