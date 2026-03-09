package com.example.demo.analystics.infrastructure.persistence.adapter.writer.candle;

import com.example.demo.analystics.application.port.out.FlushAndSaveAnalyticValuePort;
import com.example.demo.analystics.domain.domain.candle.close.PremiumDetailCloseCandle;
import com.example.demo.analystics.infrastructure.persistence.adapter.base.WriteAnalyticsAdapter;
import com.example.demo.analystics.infrastructure.persistence.entity.candle.PremiumDetailCandleEntity;
import com.example.demo.infra_shard.persistence.DomainToEntity;
import org.springframework.stereotype.Component;

@Component
public class PremiumDetailCandleWriteAdapter extends WriteAnalyticsAdapter<PremiumDetailCloseCandle, PremiumDetailCandleEntity> {

    public PremiumDetailCandleWriteAdapter(FlushAndSaveAnalyticValuePort<PremiumDetailCandleEntity> adapter,
                                           DomainToEntity<PremiumDetailCloseCandle, PremiumDetailCandleEntity> mapper) {
        super(adapter, mapper);
    }
}
