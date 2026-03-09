package com.example.demo.analystics.infrastructure.persistence.mapper.indicator;

import com.example.demo.analystics.domain.domain.indicator.close.PremiumCloseIndicator;
import com.example.demo.analystics.infrastructure.persistence.entity.indicator.PremiumIndicatorEntity;
import com.example.demo.infra_shard.persistence.DomainToEntity;
import org.springframework.stereotype.Component;

@Component
public class PremiumIndicatorEntityMapper implements DomainToEntity<PremiumCloseIndicator, PremiumIndicatorEntity> {

    @Override
    public PremiumIndicatorEntity toEntity(PremiumCloseIndicator pi) {
        return PremiumIndicatorEntity.builder()
                .symbol(pi.symbol())
                .baseExchangeId(pi.baseExchangeId())
                .compareExchangeId(pi.compareExchangeId())
                .interval(pi.interval())
                .type(pi.type())
                .period(pi.period())
                .value(pi.value())
                .bucketOpenTs(pi.times().bucketOpenTs())
                .bucketCloseTs(pi.times().bucketCloseTs())
                .observeOpenTs(pi.times().observeOpenTs())
                .observeCloseTs(pi.times().observeCloseTs())
                .build();
    }
}
