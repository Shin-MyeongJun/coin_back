package com.example.demo.analytics_query.infrastructure.persistence.mapper;

import com.example.demo.analytics_query.application.dto.PremiumCandleView;
import com.example.demo.analytics_query.infrastructure.persistence.entity.PremiumCandleQueryEntity;
import org.springframework.stereotype.Component;

@Component
public class PremiumCandleViewMapper {

    public PremiumCandleView toView(PremiumCandleQueryEntity e) {
        return new PremiumCandleView(
                e.getSymbol(), e.getBaseExchangeId(), e.getCompareExchangeId(),
                e.getInterval(), e.getOpen(), e.getHigh(), e.getLow(), e.getClose(),
                e.getBucketOpenTs(), e.getBucketCloseTs(),
                e.getObserveOpenTs(), e.getObserveCloseTs()
        );
    }
}
