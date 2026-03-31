package com.example.demo.analystics.infrastructure.scheduler;

import com.example.demo.analystics.application.port.in.PremiumDetailAnalyticsUseCase;
import com.example.demo.analystics.domain.domain.Interval;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PremiumDetailAnalyticsDbScheduler  extends AnalyticsDbScheduler  {

    private final PremiumDetailAnalyticsUseCase useCase;

    @Override
    public void process(Interval interval) {
        useCase.flushCandles(Interval.M1);
    }
}