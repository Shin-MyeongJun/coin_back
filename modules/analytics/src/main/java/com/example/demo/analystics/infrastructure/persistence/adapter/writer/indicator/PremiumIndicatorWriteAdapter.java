package com.example.demo.analystics.infrastructure.persistence.adapter.writer.indicator;

import com.example.demo.analystics.application.port.out.FlushAndSaveAnalyticValuePort;
import com.example.demo.analystics.domain.domain.indicator.close.PremiumCloseIndicator;
import com.example.demo.analystics.infrastructure.persistence.adapter.base.WriteAnalyticsAdapter;
import com.example.demo.analystics.infrastructure.persistence.entity.indicator.PremiumIndicatorEntity;
import com.example.demo.infra_shard.persistence.DomainToEntity;
import org.springframework.stereotype.Component;

@Component
public class PremiumIndicatorWriteAdapter extends WriteAnalyticsAdapter<PremiumCloseIndicator, PremiumIndicatorEntity> {

    public PremiumIndicatorWriteAdapter(FlushAndSaveAnalyticValuePort<PremiumIndicatorEntity> adapter, DomainToEntity<PremiumCloseIndicator, PremiumIndicatorEntity> mapper) {
        super(adapter, mapper);
    }
}