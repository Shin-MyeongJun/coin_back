package com.example.demo.analystics.application.usecase.caching;

import com.example.demo.analystics.application.port.in.PartitionCachingUseCase;
import com.example.demo.analystics.application.port.out.WriteAnalyticsStatePort;
import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.open.PremiumDetailCandle;
import com.example.demo.analystics.domain.partition_registry.PremiumDetailPartitionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CachingPremiumDetailStateService implements PartitionCachingUseCase {
    private final PremiumDetailPartitionRegistry registry;
    private final WriteAnalyticsStatePort<PremiumDetailCandle> candleWritePort;


    @Override
    public void caching(int partitionId) {
        for (Interval interval : Interval.analyticsSupported()) {
            candleWritePort.upsert(partitionId,interval,registry.getCandles(partitionId,interval));
        }
    }

    @Override
    public void caching() {
        for (int id : registry.getActivePartitionIds()) {
            caching(id);
        }
    }
}
