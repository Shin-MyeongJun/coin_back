package com.example.demo.analystics.application.usecase.base;

import com.example.demo.analystics.application.port.in.RestoreAnalyticsStateUseCase;
import com.example.demo.analystics.application.port.out.MappingRecoverToStatePort;
import com.example.demo.analystics.application.port.out.ReadAnalyticsStatePort;
import com.example.demo.analystics.domain.dispatch_manager.AnalyticsMangerController;
import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.key.DataKey;
import com.example.demo.analystics.domain.domain.recovery.RecoveryState;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public abstract class RestoreAnalyticsStateService<
        KEY extends DataKey<KEY>,
        RECOVER extends RecoveryState,
        TD
        >
        implements RestoreAnalyticsStateUseCase {

    private final ReadAnalyticsStatePort<KEY, RECOVER> reader;
    private final MappingRecoverToStatePort<RECOVER,KEY,TD > mapper;
    private final AnalyticsMangerController<?, ?, TD , ?> core;

    // 타임프레임 리스트를 상수로 관리하여 매번 리스트 객체가 생성되는 것을 방지합니다.
    private static final List<Interval> INTERVAL_LIST = List.of(
            Interval.M1, Interval.M3, Interval.M5,
            Interval.M15, Interval.M30, Interval.M60, Interval.M240
    );


    @Override
    public void restore(List<Integer> partitionIds) {
        if (partitionIds == null || partitionIds.isEmpty()) return;

        for (Integer partitionId : partitionIds) {
            Map<Interval, List<TD>> partitionCandles = new HashMap<>();
            // 2. Stream 대신 for문을 사용하여 직관적으로 처리
            for (Interval interval : INTERVAL_LIST) {
                Map<KEY, RECOVER> stateMap = reader.read(partitionId, interval);

                // 데이터가 있을 때만 변환해서 Map에 넣음
                if (stateMap != null && !stateMap.isEmpty()) {
                    List<TD > candles = stateMap.entrySet().stream()
                            .map(entry -> mapper.toState(entry.getKey(), entry.getValue()))
                            .toList();
                    partitionCandles.put(interval, candles);
                }
            }
            // 3. 복구된 데이터가 하나라도 있다면(혹은 콜드스타트를 위해 비어있어도) 할당
            core.assignPartition(partitionId, partitionCandles);
        }
    }
}
