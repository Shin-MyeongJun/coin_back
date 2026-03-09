package com.example.demo.analystics.application.usecase.base.interval_flush;

import com.example.demo.analystics.application.kernel.base.CandleManagerController;
import com.example.demo.analystics.application.port.in.IntervalFlushUseCase;
import com.example.demo.analystics.application.port.out.WriteAnalyticsValuePort;
import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.close.CloseCandle;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class FlushCandleService<CLOSE_CANDLE extends CloseCandle> implements IntervalFlushUseCase {

    private final CandleManagerController<?,?,?,CLOSE_CANDLE,?,?> controller;
    private final WriteAnalyticsValuePort<CLOSE_CANDLE> writePort;

    @Override
    public void flush(Interval interval) {
        writePort.write(controller.flush(interval));
    }
}
