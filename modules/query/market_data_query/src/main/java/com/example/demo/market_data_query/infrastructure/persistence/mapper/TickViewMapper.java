package com.example.demo.market_data_query.infrastructure.persistence.mapper;

import com.example.demo.market_data_query.application.dto.TickLatestView;
import com.example.demo.market_data_query.infrastructure.persistence.entity.TickQueryEntity;
import org.springframework.stereotype.Component;

@Component
public class TickViewMapper {

    public TickLatestView toLatestView(TickQueryEntity e) {
        return new TickLatestView(e.getId(), e.getMarketCodeId(), e.getTimestamp(), e.getBid(), e.getAsk());
    }
}
