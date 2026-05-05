package com.example.demo.analytics_query.infrastructure.persistence.mapper;

import com.example.demo.analytics_query.application.dto.TickIndicatorView;
import com.example.demo.analytics_query.infrastructure.persistence.entity.TickIndicatorQueryEntity;
import org.springframework.stereotype.Component;

@Component
public class TickIndicatorViewMapper {

    public TickIndicatorView toView(TickIndicatorQueryEntity e) {
        return new TickIndicatorView(
                e.getMarketCodeId(), e.getInterval(), e.getType(), e.getPeriod(), e.getValue(),
                e.getBucketOpenTs(), e.getBucketCloseTs(),
                e.getObserveOpenTs(), e.getObserveCloseTs()
        );
    }
}
