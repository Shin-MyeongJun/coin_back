package com.example.demo.analystics.application.usecase.restore;

import com.example.demo.analystics.application.port.in.RestoreAnalyticsStateUseCase;
import com.example.demo.analystics.application.port.out.MappingRecoverToStatePort;
import com.example.demo.analystics.application.port.out.ReadAnalyticsStatePort;
import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.open.PremiumCandle;
import com.example.demo.analystics.domain.domain.indicator.open.PremiumIndicator;
import com.example.demo.analystics.domain.domain.key.IndicatorPriceDataKey;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.domain.recovery.RecoveryCandleState;
import com.example.demo.analystics.domain.domain.recovery.RecoveryIndicatorState;
import com.example.demo.analystics.domain.partition_registry.PremiumPartitionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PremiumRestoreService  implements RestoreAnalyticsStateUseCase {

    private final PremiumPartitionRegistry registry;

    // Candle 복원용 포트
    private final ReadAnalyticsStatePort<PremiumKey, RecoveryCandleState<BigDecimal>> candleReader;
    private final MappingRecoverToStatePort<RecoveryCandleState<BigDecimal>, PremiumKey, PremiumCandle> candleMapper;

    // Indicator 복원용 포트
    private final ReadAnalyticsStatePort<IndicatorPriceDataKey<PremiumKey>, RecoveryIndicatorState> indicatorReader;
    private final MappingRecoverToStatePort<RecoveryIndicatorState, IndicatorPriceDataKey<PremiumKey>, PremiumIndicator> indicatorMapper;

    @Override
    public void restore(List<Integer> partitionIds) {
        if (partitionIds == null || partitionIds.isEmpty()) return;

        for (int partitionId : partitionIds) {
            registry.assignPartition(partitionId);

            // Candle 복원
            Map<Interval, List<PremiumCandle>> candleSnapshot = new HashMap<>();
            for (Interval interval : Interval.analyticsSupported()) {
                Map<PremiumKey, RecoveryCandleState<BigDecimal>> stateMap =
                        candleReader.read(partitionId, interval);
                if (stateMap != null && !stateMap.isEmpty()) {
                    candleSnapshot.put(interval, stateMap.entrySet().stream()
                            .map(e -> candleMapper.toState(e.getKey(), e.getValue()))
                            .toList());
                }
            }
            registry.restoreCandles(partitionId, candleSnapshot);

            // Indicator 복원
            Map<Interval, List<PremiumIndicator>> indicatorSnapshot = new HashMap<>();
            for (Interval interval : Interval.analyticsSupported()) {
                Map<IndicatorPriceDataKey<PremiumKey>, RecoveryIndicatorState> stateMap =
                        indicatorReader.read(partitionId, interval);
                if (stateMap != null && !stateMap.isEmpty()) {
                    indicatorSnapshot.put(interval, stateMap.entrySet().stream()
                            .map(e -> indicatorMapper.toState(e.getKey(), e.getValue()))
                            .toList());
                }
            }
            registry.restoreIndicators(partitionId, indicatorSnapshot);
        }
    }
}