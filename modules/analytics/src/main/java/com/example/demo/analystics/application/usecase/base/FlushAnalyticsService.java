package com.example.demo.analystics.application.usecase.base;

import com.example.demo.analystics.application.port.in.IntervalFlushUseCase;
import com.example.demo.analystics.application.port.out.WriteAnalyticsValuePort;
import com.example.demo.analystics.domain.dispatch_manager.AnalyticsMangerController;
import com.example.demo.analystics.domain.domain.Interval;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class FlushAnalyticsService<CLOSE_TD> implements IntervalFlushUseCase {

    private final AnalyticsMangerController<?,?,?,CLOSE_TD> controller;
    private final WriteAnalyticsValuePort<CLOSE_TD> writePort;

    @Override
    public void flush(Interval interval) {
        writePort.write(controller.flush(interval));
    }
}
