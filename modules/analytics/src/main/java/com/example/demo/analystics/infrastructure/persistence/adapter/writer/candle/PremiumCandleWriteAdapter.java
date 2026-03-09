package com.example.demo.analystics.infrastructure.persistence.adapter.writer.candle;

import com.example.demo.analystics.application.port.out.FlushAndSaveAnalyticValuePort;
import com.example.demo.analystics.domain.domain.candle.close.PremiumCloseCandle;
import com.example.demo.analystics.infrastructure.persistence.adapter.base.WriteAnalyticsAdapter;
import com.example.demo.analystics.infrastructure.persistence.entity.candle.PremiumCandleEntity;
import com.example.demo.infra_shard.persistence.DomainToEntity;
import org.springframework.stereotype.Component;

@Component
public class PremiumCandleWriteAdapter extends WriteAnalyticsAdapter<PremiumCloseCandle, PremiumCandleEntity> {

    public PremiumCandleWriteAdapter(FlushAndSaveAnalyticValuePort<PremiumCandleEntity> adapter,
                                     DomainToEntity<PremiumCloseCandle, PremiumCandleEntity> mapper) {
        super(adapter, mapper);
    }
}