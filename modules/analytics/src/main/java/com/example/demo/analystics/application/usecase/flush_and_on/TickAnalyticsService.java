package com.example.demo.analystics.application.usecase.flush_and_on;

import com.example.demo.analystics.application.port.in.TickAnalyticsUseCase;
import com.example.demo.analystics.application.port.out.WriteAnalyticsValuePort;
import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.close.TickCloseCandle;
import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.analystics.domain.partition_registry.TickPartitionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TickAnalyticsService implements TickAnalyticsUseCase {

    private final TickPartitionRegistry registry;
    private final WriteAnalyticsValuePort<TickCloseCandle> candleWriter;
    private final WriteAnalyticsValuePort<com.example.demo.analystics.domain.domain.indicator.close.TickCloseIndicator> indicatorWriter;

    @Override
    public void onData(int partitionId,  TickKey key, BigDecimal price) {
        registry.update(partitionId, key, price);
    }

    @Override
    public void flushCandles(Interval interval) {
        var closed = registry.flushCandles(interval);
        if (!closed.isEmpty()) {
            candleWriter.write(closed);
        }
    }

    @Override
    public void flushIndicators(Interval interval) {
        var closed = registry.flushIndicators(interval);
        if (!closed.isEmpty()) {
            indicatorWriter.write(closed);
        }
    }
}
