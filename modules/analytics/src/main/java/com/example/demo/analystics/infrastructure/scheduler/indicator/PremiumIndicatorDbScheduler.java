package com.example.demo.analystics.infrastructure.scheduler.indicator;

import com.example.demo.analystics.application.port.in.IntervalFlushUseCase;
import com.example.demo.analystics.infrastructure.scheduler.AnalyticsDbScheduler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class PremiumIndicatorDbScheduler extends AnalyticsDbScheduler {
    public PremiumIndicatorDbScheduler(@Qualifier("flushPremiumIndicatorService") IntervalFlushUseCase useCase) {
        super(useCase);
    }
}
