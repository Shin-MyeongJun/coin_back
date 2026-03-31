package com.example.demo.analystics.infrastructure.scheduler;

import com.example.demo.analystics.application.port.in.TickAnalyticsUseCase;
import com.example.demo.analystics.domain.domain.Interval;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TickAnalyticsDbScheduler extends AnalyticsDbScheduler  {
    private final TickAnalyticsUseCase useCase;

    @Override
    public void process(Interval interval) {
        useCase.flushCandles(interval);
        useCase.flushIndicators(interval);
    }

}