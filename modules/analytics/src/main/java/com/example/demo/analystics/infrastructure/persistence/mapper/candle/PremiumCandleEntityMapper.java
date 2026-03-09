package com.example.demo.analystics.infrastructure.persistence.mapper.candle;

import com.example.demo.analystics.domain.domain.candle.close.PremiumCloseCandle;
import com.example.demo.analystics.infrastructure.persistence.entity.candle.PremiumCandleEntity;
import com.example.demo.infra_shard.persistence.DomainToEntity;
import org.springframework.stereotype.Component;

@Component
public class PremiumCandleEntityMapper implements DomainToEntity<PremiumCloseCandle, PremiumCandleEntity> {


    @Override
    public PremiumCandleEntity toEntity(PremiumCloseCandle pc) {
        return PremiumCandleEntity.builder()
                .symbol(pc.symbol())
                .baseExchangeId(pc.baseExchangeId())
                .compareExchangeId(pc.compareExchangeId())
                .interval(pc.interval())
                .open(pc.open())
                .high(pc.high())
                .low(pc.low())
                .close(pc.close())
                .bucketOpenTs(pc.times().bucketOpenTs())
                .bucketCloseTs(pc.times().bucketCloseTs())
                .observeOpenTs(pc.times().observeOpenTs())
                .observeCloseTs(pc.times().observeCloseTs())
                .build();
    }
}
