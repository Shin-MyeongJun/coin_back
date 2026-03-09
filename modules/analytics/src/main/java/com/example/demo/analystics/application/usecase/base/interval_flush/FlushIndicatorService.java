package com.example.demo.analystics.application.usecase.base.interval_flush;

import com.example.demo.analystics.application.kernel.base.DispatchIndicatorManager;
import com.example.demo.analystics.application.port.in.IntervalFlushUseCase;
import com.example.demo.analystics.domain.domain.Interval;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class FlushIndicatorService implements IntervalFlushUseCase {

    private final DispatchIndicatorManager<?,?,?,?> core;

    @Override
    public void flush(Interval interval) {
        core.flush(interval);
    }
}
