package com.example.demo.analytics_query.infrastructure.persistence.mapper;

import com.example.demo.analytics_query.application.dto.CandleView;
import com.example.demo.analytics_query.infrastructure.persistence.entity.PremiumCandleQueryEntity;
import com.example.demo.analytics_query.infrastructure.persistence.entity.TickCandleQueryEntity;
import org.springframework.stereotype.Component;

@Component
public class CandleViewMapper {

    public CandleView fromTickCandle(TickCandleQueryEntity e) {
        return new CandleView(
                e.getId(), e.getMarketCodeId(), null, null, null,
                e.getInterval(), e.getOpen(), e.getHigh(), e.getLow(), e.getClose(),
                e.getBucketOpenTs(), e.getBucketCloseTs(), e.getObserveOpenTs(), e.getObserveCloseTs()
        );
    }

    public CandleView fromPremiumCandle(PremiumCandleQueryEntity e) {
        return new CandleView(
                e.getId(), null, e.getSymbol(), e.getBaseExchangeId(), e.getCompareExchangeId(),
                e.getInterval(), e.getOpen(), e.getHigh(), e.getLow(), e.getClose(),
                e.getBucketOpenTs(), e.getBucketCloseTs(), e.getObserveOpenTs(), e.getObserveCloseTs()
        );
    }
}
