package com.example.demo.analystics.infrastructure.persistence.mapper.candle;

import com.example.demo.analystics.domain.domain.candle.close.TickCloseCandle;
import com.example.demo.analystics.infrastructure.persistence.entity.candle.TickCandleEntity;
import com.example.demo.infra_shard.persistence.DomainToEntity;
import org.springframework.stereotype.Component;

@Component
public class TickCandleEntityMapper implements DomainToEntity<TickCloseCandle, TickCandleEntity> {


    @Override
    public TickCandleEntity toEntity(TickCloseCandle tc) {
        return TickCandleEntity.builder()
                .marketCodeId(tc.marketCodeId())
                .interval(tc.interval())
                .open(tc.open())
                .high(tc.high())
                .low(tc.low())
                .close(tc.close())
                .bucketOpenTs(tc.times().bucketOpenTs())
                .bucketCloseTs(tc.times().bucketCloseTs())
                .observeOpenTs(tc.times().observeOpenTs())
                .observeCloseTs(tc.times().observeCloseTs())
                .build();
    }
}
