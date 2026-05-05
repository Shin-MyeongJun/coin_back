package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.analytics_query.application.dto.ScreenerResult;
import com.example.demo.analytics_query.application.port.out.GetScreenerPort;
import com.example.demo.analytics_query.infrastructure.persistence.querydsl.IndicatorQueryDslRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GetScreenerAdapter implements GetScreenerPort {

    private final IndicatorQueryDslRepository queryDslRepository;

    @Override
    public List<ScreenerResult> findByIndicatorCondition(String interval, String type, BigDecimal minValue, BigDecimal maxValue) {
        return queryDslRepository.findByIndicatorCondition(interval, type, minValue, maxValue);
    }
}
