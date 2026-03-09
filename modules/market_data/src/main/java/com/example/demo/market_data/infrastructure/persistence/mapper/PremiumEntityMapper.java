package com.example.demo.market_data.infrastructure.persistence.mapper;

import com.example.demo.infra_shard.persistence.EntityMapping;
import com.example.demo.market_data.domain.domain.Premium;
import com.example.demo.market_data.infrastructure.persistence.entity.PremiumEntity;
import org.springframework.stereotype.Component;

@Component
public class PremiumEntityMapper implements EntityMapping<Premium, PremiumEntity> {
    @Override
    public PremiumEntity toEntity(Premium p) {
        return PremiumEntity.builder()
                .symbol(p.symbol())
                .baseExchangeId(p.baseExchangeId())
                .compareExchangeId(p.compareExchangeId())
                .bid(p.bid())
                .ask(p.ask())
                .timestamp(p.timestamp())
                .build();
    }

    @Override
    public Premium toDomain(PremiumEntity pe) {
        return new Premium(
                pe.getSymbol(),
                pe.getBaseExchangeId(),
                pe.getCompareExchangeId(),
                pe.getBid(),
                pe.getAsk(),
                pe.getTimestamp()
        );
    }
}
