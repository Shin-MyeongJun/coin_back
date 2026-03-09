package com.example.demo.analystics.infrastructure.persistence.mapper.candle;

import com.example.demo.analystics.domain.domain.candle.close.PremiumDetailCloseCandle;
import com.example.demo.analystics.domain.domain.candle.value.PremiumDetailValue;
import com.example.demo.analystics.infrastructure.persistence.entity.candle.PremiumDetailCandleEntity;
import com.example.demo.analystics.infrastructure.persistence.entity.candle.PremiumDetailValueEmbeddable;
import com.example.demo.infra_shard.persistence.DomainToEntity;
import org.springframework.stereotype.Component;

@Component
public class PremiumDetailCandleEntityMapper implements DomainToEntity<PremiumDetailCloseCandle, PremiumDetailCandleEntity> {

    @Override
    public PremiumDetailCandleEntity toEntity(PremiumDetailCloseCandle pdc) {
        PremiumDetailValueEmbeddable open = toEmbeddable(pdc.open());
        PremiumDetailValueEmbeddable high = toEmbeddable(pdc.high());
        PremiumDetailValueEmbeddable low = toEmbeddable(pdc.low());
        PremiumDetailValueEmbeddable close = toEmbeddable(pdc.close());




        return PremiumDetailCandleEntity.builder()
                .symbol(pdc.symbol())
                .baseExchangeId(pdc.baseExchangeId())
                .compareExchangeId(pdc.compareExchangeId())
                .interval(pdc.interval())
                .open(open)
                .high(high)
                .low(low)
                .close(close)
                .bucketOpenTs(pdc.times().bucketOpenTs())
                .bucketCloseTs(pdc.times().bucketCloseTs())
                .observeOpenTs(pdc.times().observeOpenTs())
                .observeCloseTs(pdc.times().observeCloseTs())
                .build();
    }



    private PremiumDetailValueEmbeddable toEmbeddable(PremiumDetailValue val) {
        return new PremiumDetailValueEmbeddable(
                val.baseVal(),
                val.baseQuoteVal(),
                val.compareVal(),
                val.compareQuoteVal()
        );
    }


}
