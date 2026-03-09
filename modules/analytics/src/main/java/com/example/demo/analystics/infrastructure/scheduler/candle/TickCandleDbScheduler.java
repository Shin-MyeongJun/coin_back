package com.example.demo.analystics.infrastructure.scheduler.candle;

import com.example.demo.analystics.application.port.in.IntervalFlushUseCase;
import com.example.demo.analystics.infrastructure.scheduler.AnalyticsDbScheduler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TickCandleDbScheduler extends AnalyticsDbScheduler {
    public TickCandleDbScheduler(@Qualifier("flushTickCandleService") IntervalFlushUseCase useCase) {
        super(useCase);
    }
}
