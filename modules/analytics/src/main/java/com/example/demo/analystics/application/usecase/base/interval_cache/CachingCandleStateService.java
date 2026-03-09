package com.example.demo.analystics.application.usecase.base.interval_cache;


import com.example.demo.analystics.application.kernel.base.CandleManagerController;
import com.example.demo.analystics.application.port.in.PartitionCachingUseCase;
import com.example.demo.analystics.application.port.out.WriteAnalyticsStatePort;
import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.open.OpenCandle;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public abstract class CachingCandleStateService<CANDLE extends OpenCandle<?,?>> implements PartitionCachingUseCase {
    private final CandleManagerController<?,?,CANDLE,?,?,?> controller;
    private final WriteAnalyticsStatePort<CANDLE> writePort;

    //주요 IntervalList
    private final List<Interval> intervalList = List.of(
            Interval.M1,Interval.M3,Interval.M5,Interval.M15,Interval.M30,Interval.M60,Interval.M240
    );

    @Override
    public void caching(int partitionId) {
        for (Interval interval : intervalList) {
            writePort.upsert(partitionId,interval,controller.get(partitionId,interval));
        }
    }
}
