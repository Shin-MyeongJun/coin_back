package com.example.demo.analystics.application.usecase.flush_and_on;

import com.example.demo.analystics.application.port.in.PublishAnalyticsDataUseCase;
import com.example.demo.analystics.application.port.in.TickAnalyticsUseCase;
import com.example.demo.analystics.application.port.out.WriteAnalyticsValuePort;
import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.close.TickCloseCandle;
import com.example.demo.analystics.domain.domain.indicator.close.TickCloseIndicator;
import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.analystics.domain.partition_registry.TickPartitionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TickAnalyticsService implements TickAnalyticsUseCase {

    private final TickPartitionRegistry registry;
    private final WriteAnalyticsValuePort<TickCloseCandle> candleWriter;
    private final WriteAnalyticsValuePort<com.example.demo.analystics.domain.domain.indicator.close.TickCloseIndicator> indicatorWriter;

    private final PublishAnalyticsDataUseCase<TickCloseCandle> candlePublisher;
    private final PublishAnalyticsDataUseCase<TickCloseIndicator> indicatorPublisher;

    @Override
    public void onData(int partitionId,  TickKey key, BigDecimal price) {
        registry.update(partitionId, key, price);
    }

    @Override
    @Transactional
    public void flushCandles(Interval interval) {
        var closed = registry.flushCandles(interval);
        if (!closed.isEmpty()) {
            candleWriter.write(closed);
            for (TickCloseCandle candle : closed) {
                candlePublisher.publish(candle);
            }
        }
    }

    @Override
    @Transactional
    public void flushIndicators(Interval interval) {
        var closed = registry.flushIndicators(interval);
        if (!closed.isEmpty()) {
            indicatorWriter.write(closed);
            for (TickCloseIndicator candle : closed) {
                indicatorPublisher.publish(candle);
            }
        }
    }
}
