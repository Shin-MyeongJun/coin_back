package com.example.demo.meta_data_query.infrastructure.persistence.mapper;

import com.example.demo.infra_shard.persistence.EntityToDomain;
import com.example.demo.meta_data_query.application.dto.ExchangeView;
import com.example.demo.meta_data_query.infrastructure.persistence.entity.ExchangeQueryEntity;
import org.springframework.stereotype.Component;

@Component
public class ExchangeViewMapper implements EntityToDomain<ExchangeQueryEntity, ExchangeView> {

    @Override
    public ExchangeView toDomain(ExchangeQueryEntity entity) {
        return new ExchangeView(
                entity.getId(),
                entity.getName(),
                entity.getExchangeType(),
                entity.getQuote(),
                entity.getCountry(),
                entity.getStatus()
        );
    }
}
