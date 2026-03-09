package com.example.demo.analystics.infrastructure.persistence.adapter.writer.indicator;

import com.example.demo.analystics.application.port.out.FlushAndSaveAnalyticValuePort;
import com.example.demo.analystics.domain.domain.indicator.close.TickCloseIndicator;
import com.example.demo.analystics.infrastructure.persistence.adapter.base.WriteAnalyticsAdapter;
import com.example.demo.analystics.infrastructure.persistence.entity.indicator.TickIndicatorEntity;
import com.example.demo.infra_shard.persistence.DomainToEntity;
import org.springframework.stereotype.Component;

@Component
public class TickIndicatorWriteAdapter extends WriteAnalyticsAdapter<TickCloseIndicator, TickIndicatorEntity> {

    public TickIndicatorWriteAdapter(FlushAndSaveAnalyticValuePort<TickIndicatorEntity> adapter, DomainToEntity<TickCloseIndicator, TickIndicatorEntity> mapper) {
        super(adapter, mapper);
    }
}
