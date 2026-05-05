package com.example.demo.meta_data_query.infrastructure.persistence.mapper;

import com.example.demo.infra_shard.persistence.EntityToDomain;
import com.example.demo.meta_data_query.application.dto.MarketCodeView;
import com.example.demo.meta_data_query.infrastructure.persistence.entity.MarketCodeQueryEntity;
import org.springframework.stereotype.Component;

@Component
public class MarketCodeViewMapper implements EntityToDomain<MarketCodeQueryEntity, MarketCodeView> {

    @Override
    public MarketCodeView toDomain(MarketCodeQueryEntity entity) {
        return new MarketCodeView(
                entity.getId(),
                entity.getExchangeId(),
                entity.getBase(),
                entity.getQuote(),
                entity.getTradingPair()
        );
    }
}
