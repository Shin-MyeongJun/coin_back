package com.example.demo.analystics.application.usecase.revoke;

import com.example.demo.analystics.application.port.in.RevokeAnalyticsStateUseCase;
import com.example.demo.analystics.application.port.out.WriteAnalyticsStatePort;
import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.open.PremiumCandle;
import com.example.demo.analystics.domain.domain.indicator.open.PremiumIndicator;
import com.example.demo.analystics.domain.partition_registry.PremiumPartitionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PremiumRevokeService implements RevokeAnalyticsStateUseCase {

    private final PremiumPartitionRegistry registry;
    private final WriteAnalyticsStatePort<PremiumCandle> candleStateWriter;
    private final WriteAnalyticsStatePort<PremiumIndicator> indicatorStateWriter;

    @Override
    public void revoke(List<Integer> partitionIds) {
        if (partitionIds == null || partitionIds.isEmpty()) return;

        for (int partitionId : partitionIds) {
            // 1. 모든 Interval의 상태를 Redis에 저장
            for (Interval interval : Interval.analyticsSupported()) {
                var candles = registry.getCandles(partitionId, interval);
                if (!candles.isEmpty()) {
                    candleStateWriter.upsert(partitionId, interval, candles);
                }

                var indicators = registry.getIndicators(partitionId, interval);
                if (!indicators.isEmpty()) {
                    indicatorStateWriter.upsert(partitionId, interval, indicators);
                }
            }
        }

        // 2. Registry에서 파티션 제거
        registry.revokePartitions(partitionIds);
    }
}
