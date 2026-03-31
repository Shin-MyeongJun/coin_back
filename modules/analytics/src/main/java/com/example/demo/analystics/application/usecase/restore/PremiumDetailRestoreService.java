package com.example.demo.analystics.application.usecase.restore;

import com.example.demo.analystics.application.port.in.RestoreAnalyticsStateUseCase;
import com.example.demo.analystics.application.port.out.MappingRecoverToStatePort;
import com.example.demo.analystics.application.port.out.ReadAnalyticsStatePort;
import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.open.PremiumDetailCandle;
import com.example.demo.analystics.domain.domain.candle.value.PremiumDetailValue;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.domain.recovery.RecoveryCandleState;
import com.example.demo.analystics.domain.partition_registry.PremiumDetailPartitionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PremiumDetailRestoreService   implements RestoreAnalyticsStateUseCase {

    private final PremiumDetailPartitionRegistry registry;

    // Candle 복원용 포트
    private final ReadAnalyticsStatePort<PremiumKey, RecoveryCandleState<PremiumDetailValue>> candleReader;
    private final MappingRecoverToStatePort<RecoveryCandleState<PremiumDetailValue>, PremiumKey, PremiumDetailCandle> candleMapper;

   

    @Override
    public void restore(List<Integer> partitionIds) {
        if (partitionIds == null || partitionIds.isEmpty()) return;

        for (int partitionId : partitionIds) {
            registry.assignPartition(partitionId);

            // Candle 복원
            Map<Interval, List<PremiumDetailCandle>> candleSnapshot = new HashMap<>();
            for (Interval interval : Interval.analyticsSupported()) {
                Map<PremiumKey, RecoveryCandleState<PremiumDetailValue>> stateMap =
                        candleReader.read(partitionId, interval);
                if (stateMap != null && !stateMap.isEmpty()) {
                    candleSnapshot.put(interval, stateMap.entrySet().stream()
                            .map(e -> candleMapper.toState(e.getKey(), e.getValue()))
                            .toList());
                }
            }
            registry.restoreCandles(partitionId, candleSnapshot);
            
        }
    }
}
