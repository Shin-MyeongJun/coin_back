package com.example.demo.analytics_query.infrastructure.persistence.mapper;

import com.example.demo.analytics_query.application.dto.IndicatorView;
import com.example.demo.analytics_query.infrastructure.persistence.entity.PremiumIndicatorQueryEntity;
import com.example.demo.analytics_query.infrastructure.persistence.entity.TickIndicatorQueryEntity;
import org.springframework.stereotype.Component;

@Component
public class IndicatorViewMapper {

    public IndicatorView fromTickIndicator(TickIndicatorQueryEntity e) {
        return new IndicatorView(
                e.getId(), e.getMarketCodeId(), null, null, null,
                e.getInterval(), e.getType(), e.getPeriod(), e.getValue(),
                e.getBucketOpenTs(), e.getBucketCloseTs(), e.getObserveOpenTs(), e.getObserveCloseTs()
        );
    }

    public IndicatorView fromPremiumIndicator(PremiumIndicatorQueryEntity e) {
        return new IndicatorView(
                e.getId(), null, e.getSymbol(), e.getBaseExchangeId(), e.getCompareExchangeId(),
                e.getInterval(), e.getType(), e.getPeriod(), e.getValue(),
                e.getBucketOpenTs(), e.getBucketCloseTs(), e.getObserveOpenTs(), e.getObserveCloseTs()
        );
    }
}
