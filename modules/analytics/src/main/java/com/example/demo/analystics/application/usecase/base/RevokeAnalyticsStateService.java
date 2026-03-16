package com.example.demo.analystics.application.usecase.base;

import com.example.demo.analystics.application.port.in.RevokeAnalyticsStateUseCase;
import com.example.demo.analystics.application.port.out.WriteAnalyticsStatePort;
import com.example.demo.analystics.domain.dispatch_manager.AnalyticsMangerController;
import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.open.OpenCandle;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public abstract class RevokeAnalyticsStateService<CANDLE extends OpenCandle<?, ?>> implements RevokeAnalyticsStateUseCase {

    private final AnalyticsMangerController<?, ?, CANDLE, ?> core;
    private final WriteAnalyticsStatePort<CANDLE> writer;

    private static final List<Interval> INTERVAL_LIST = List.of(
            Interval.M1, Interval.M3, Interval.M5,
            Interval.M15, Interval.M30, Interval.M60, Interval.M240
    );

    @Override
    public void revoke(List<Integer> partitionIds) {
        if (partitionIds == null || partitionIds.isEmpty()) {
            return;
        }

        for (Integer partitionId : partitionIds) {
            // 1. 모든 간격(Interval)에 대해 메모리 상의 캔들 데이터를 수집하여 저장
            for (Interval interval : INTERVAL_LIST) {
                List<CANDLE> candles = core.get(partitionId, interval);
                if (candles != null && !candles.isEmpty()) {
                    // 2. 외부 저장소(Port)를 통해 상태 저장 (Flush)
                    writer.upsert(partitionId, interval, candles);
                }
            }
        }

        // 3. 상태 저장이 완료된 후, 컨트롤러의 관리 맵에서 해당 파티션 매니저들을 제거
        core.revokePartitions(partitionIds);
    }
}
