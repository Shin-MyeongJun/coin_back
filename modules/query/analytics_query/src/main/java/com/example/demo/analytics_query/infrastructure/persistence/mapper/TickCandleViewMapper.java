package com.example.demo.analytics_query.infrastructure.persistence.mapper;

import com.example.demo.analytics_query.application.dto.TickCandleView;
import com.example.demo.analytics_query.infrastructure.persistence.entity.TickCandleQueryEntity;
import org.springframework.stereotype.Component;

@Component
public class TickCandleViewMapper {

    public TickCandleView toView(TickCandleQueryEntity e) {
        return new TickCandleView(
                e.getMarketCodeId(), e.getInterval(),
                e.getOpen(), e.getHigh(), e.getLow(), e.getClose(),
                e.getBucketOpenTs(), e.getBucketCloseTs(),
                e.getObserveOpenTs(), e.getObserveCloseTs()
        );
    }
}
