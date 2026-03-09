package com.example.demo.analystics.infrastructure.scheduler.candle;

import com.example.demo.analystics.application.port.in.IntervalFlushUseCase;
import com.example.demo.analystics.infrastructure.scheduler.AnalyticsDbScheduler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class PremiumCandleDbScheduler extends AnalyticsDbScheduler {
    public PremiumCandleDbScheduler(@Qualifier("flushPremiumCandleService")IntervalFlushUseCase useCase) {
        super(useCase);
    }
}
