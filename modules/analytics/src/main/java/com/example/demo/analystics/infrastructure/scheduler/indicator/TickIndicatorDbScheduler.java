package com.example.demo.analystics.infrastructure.scheduler.indicator;

import com.example.demo.analystics.application.port.in.IntervalFlushUseCase;
import com.example.demo.analystics.infrastructure.scheduler.AnalyticsDbScheduler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TickIndicatorDbScheduler extends AnalyticsDbScheduler {
    public TickIndicatorDbScheduler(@Qualifier("flushTickIndicatorService") IntervalFlushUseCase useCase) {
        super(useCase);
    }
}
