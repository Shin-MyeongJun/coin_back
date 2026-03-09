package com.example.demo.analystics.infrastructure.persistence.adapter.writer.candle;

import com.example.demo.analystics.application.port.out.FlushAndSaveAnalyticValuePort;
import com.example.demo.analystics.domain.domain.candle.close.TickCloseCandle;
import com.example.demo.analystics.infrastructure.persistence.adapter.base.WriteAnalyticsAdapter;
import com.example.demo.analystics.infrastructure.persistence.entity.candle.TickCandleEntity;
import com.example.demo.infra_shard.persistence.DomainToEntity;
import org.springframework.stereotype.Component;

@Component
public class TickCandleWriteAdapter extends WriteAnalyticsAdapter<TickCloseCandle, TickCandleEntity> {
    public TickCandleWriteAdapter(FlushAndSaveAnalyticValuePort<TickCandleEntity> adapter
            , DomainToEntity<TickCloseCandle, TickCandleEntity> mapper) {
        super(adapter, mapper);
    }
}
