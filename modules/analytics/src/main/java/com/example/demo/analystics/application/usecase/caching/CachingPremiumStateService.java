package com.example.demo.analystics.application.usecase.caching;

import com.example.demo.analystics.application.port.in.PartitionCachingUseCase;
import com.example.demo.analystics.application.port.out.WriteAnalyticsStatePort;
import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.open.PremiumCandle;
import com.example.demo.analystics.domain.domain.indicator.open.PremiumIndicator;
import com.example.demo.analystics.domain.partition_registry.PremiumPartitionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CachingPremiumStateService  implements PartitionCachingUseCase {
    private final PremiumPartitionRegistry registry;
    private final WriteAnalyticsStatePort<PremiumCandle> candleWritePort;
    private final WriteAnalyticsStatePort<PremiumIndicator> indWritePort;

    @Override
    public void caching(int partitionId) {
        for (Interval interval : Interval.analyticsSupported()) {
            candleWritePort.upsert(partitionId,interval,registry.getCandles(partitionId,interval));
            indWritePort.upsert(partitionId,interval,registry.getIndicators(partitionId,interval));
        }
    }

    @Override
    public void caching() {
        for (int id : registry.getActivePartitionIds()) {
            caching(id);
        }
    }
}
