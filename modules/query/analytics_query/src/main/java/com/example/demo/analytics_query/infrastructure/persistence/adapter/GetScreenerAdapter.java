package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.analytics_query.application.dto.PremiumScreenerResult;
import com.example.demo.analytics_query.application.dto.ScreenerCondition;
import com.example.demo.analytics_query.application.dto.TickScreenerResult;
import com.example.demo.analytics_query.application.port.out.GetScreenerPort;
import com.example.demo.analytics_query.infrastructure.persistence.querydsl.IndicatorQueryDslRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetScreenerAdapter implements GetScreenerPort {

    private final IndicatorQueryDslRepository queryDslRepository;

    @Override
    public List<TickScreenerResult> findTickByConditions(List<ScreenerCondition> conditions) {
        return queryDslRepository.findTickByConditions(conditions);
    }

    @Override
    public List<PremiumScreenerResult> findPremiumByConditions(List<ScreenerCondition> conditions) {
        return queryDslRepository.findPremiumByConditions(conditions);
    }
}
