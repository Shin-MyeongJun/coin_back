package com.example.demo.analystics.infrastructure.persistence.mapper.indicator;

import com.example.demo.analystics.domain.domain.indicator.close.TickCloseIndicator;
import com.example.demo.analystics.infrastructure.persistence.entity.indicator.TickIndicatorEntity;
import com.example.demo.infra_shard.persistence.DomainToEntity;
import org.springframework.stereotype.Component;

@Component
public class TickIndicatorEntityMapper implements DomainToEntity<TickCloseIndicator, TickIndicatorEntity> {
    @Override
    public TickIndicatorEntity toEntity(TickCloseIndicator ti) {
        return TickIndicatorEntity.builder()
                .marketCodeId(ti.marketCodeId())
                .interval(ti.interval())
                .type(ti.type())
                .period(ti.period())
                .value(ti.value())
                .bucketOpenTs(ti.times().bucketOpenTs())
                .bucketCloseTs(ti.times().bucketCloseTs())
                .observeOpenTs(ti.times().observeOpenTs())
                .observeCloseTs(ti.times().observeCloseTs())
                .build();
    }
}
