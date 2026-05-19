package com.example.demo.analystics.application.usecase.flush_and_on;

import com.example.demo.analystics.application.port.in.PremiumAnalyticsUseCase;
import com.example.demo.analystics.application.port.in.PublishAnalyticsDataUseCase;
import com.example.demo.analystics.application.port.out.WriteAnalyticsValuePort;
import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.close.PremiumCloseCandle;
import com.example.demo.analystics.domain.domain.indicator.close.PremiumCloseIndicator;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.partition_registry.PremiumPartitionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PremiumAnalyticsService implements PremiumAnalyticsUseCase {

    private final PremiumPartitionRegistry registry;
    private final WriteAnalyticsValuePort<PremiumCloseCandle> candleWriter;
    private final WriteAnalyticsValuePort<PremiumCloseIndicator> indicatorWriter;

    private final PublishAnalyticsDataUseCase<PremiumCloseCandle> candlePublisher;
    private final PublishAnalyticsDataUseCase<PremiumCloseIndicator> indicatorPublisher;


    @Override
    public void onData(int partitionId, PremiumKey key, BigDecimal price) {
        registry.update(partitionId, key, price);
    }

    @Override
    @Transactional
    public void flushCandles(Interval interval) {
        var closed = registry.flushCandles(interval);
        if (!closed.isEmpty()) {
            candleWriter.write(closed);
            for (PremiumCloseCandle candle : closed) {
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
            for (PremiumCloseIndicator candle : closed) {
                indicatorPublisher.publish(candle);
            }
        }
    }
}
