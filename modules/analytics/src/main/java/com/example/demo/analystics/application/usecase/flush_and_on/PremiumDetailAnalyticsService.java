package com.example.demo.analystics.application.usecase.flush_and_on;

import com.example.demo.analystics.application.port.in.PremiumDetailAnalyticsUseCase;
import com.example.demo.analystics.application.port.in.PublishAnalyticsDataUseCase;
import com.example.demo.analystics.application.port.out.WriteAnalyticsValuePort;
import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.close.PremiumDetailCloseCandle;
import com.example.demo.analystics.domain.domain.candle.value.PremiumDetailValue;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.partition_registry.PremiumDetailPartitionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PremiumDetailAnalyticsService implements PremiumDetailAnalyticsUseCase {

    private final PremiumDetailPartitionRegistry registry;
    private final WriteAnalyticsValuePort<PremiumDetailCloseCandle> candleWriter;
    private final PublishAnalyticsDataUseCase<PremiumDetailCloseCandle> candlePublisher;

    @Override
    public void onData(int partitionId, PremiumKey key, PremiumDetailValue value) {
        registry.update(partitionId, key, value);
    }

    @Override
    public void flushCandles(Interval interval) {
        var closed = registry.flushCandles(interval);
        if (!closed.isEmpty()) {
            candleWriter.write(closed);
            for (PremiumDetailCloseCandle candle : closed) {
                candlePublisher.publish(candle);
            }
        }
    }
}
