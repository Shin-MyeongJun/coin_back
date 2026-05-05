package com.example.demo.economic_query.application.port.out;

import com.example.demo.economic_query.application.dto.IndicatorChangeRateView;

import java.util.List;

public interface GetIndicatorChangeRatePort {
    List<IndicatorChangeRateView> findByIndCodeId(Long indCodeId);
}
