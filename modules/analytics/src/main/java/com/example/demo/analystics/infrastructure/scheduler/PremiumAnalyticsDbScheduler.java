package com.example.demo.analystics.infrastructure.scheduler;

import com.example.demo.analystics.application.port.in.PremiumAnalyticsUseCase;
import com.example.demo.analystics.domain.domain.Interval;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PremiumAnalyticsDbScheduler extends AnalyticsDbScheduler {

    private final PremiumAnalyticsUseCase useCase;

    public void process(Interval interval) {
        useCase.flushCandles(interval);
        useCase.flushIndicators(interval);
    }
}
