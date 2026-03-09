package com.example.demo.market_data.infrastructure.persistence.mapper;

import com.example.demo.infra_shard.persistence.EntityMapping;
import com.example.demo.market_data.domain.domain.Tick;
import com.example.demo.market_data.infrastructure.persistence.entity.TickEntity;
import org.springframework.stereotype.Component;

@Component
public class TickEntityMapper implements EntityMapping<Tick, TickEntity> {
    @Override
    public TickEntity toEntity(Tick t) {
        return TickEntity.builder()
                .marketCodeId(t.marketCodeId())
                .bid(t.bid())
                .ask(t.ask())
                .timestamp(t.timestamp())
                .build();
    }

    @Override
    public Tick toDomain(TickEntity te) {
        return new Tick(
                te.getMarketCodeId(),
                te.getBid(),
                te.getAsk(),
                te.getTimestamp()
        );
    }
}
