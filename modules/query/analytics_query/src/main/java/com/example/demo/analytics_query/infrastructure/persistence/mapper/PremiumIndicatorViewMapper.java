package com.example.demo.analytics_query.infrastructure.persistence.mapper;

import com.example.demo.analytics_query.application.dto.PremiumIndicatorView;
import com.example.demo.analytics_query.infrastructure.persistence.entity.PremiumIndicatorQueryEntity;
import org.springframework.stereotype.Component;

@Component
public class PremiumIndicatorViewMapper {

    public PremiumIndicatorView toView(PremiumIndicatorQueryEntity e) {
        return new PremiumIndicatorView(
                e.getSymbol(), e.getBaseExchangeId(), e.getCompareExchangeId(),
                e.getInterval(), e.getType(), e.getPeriod(), e.getValue(),
                e.getBucketOpenTs(), e.getBucketCloseTs(),
                e.getObserveOpenTs(), e.getObserveCloseTs()
        );
    }
}
