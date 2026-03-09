package com.example.demo.analystics.application.usecase.base.restore;

import com.example.demo.analystics.application.port.in.RestoreAnalyticsStateUseCase;
import com.example.demo.analystics.application.port.out.ReadAnalyticsStatePort;
import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.key.DataKey;
import com.example.demo.analystics.domain.domain.recovery.RecoveryState;

import java.util.List;
import java.util.Map;

public abstract class RestoreAnalyticsDataService<
        KEY extends DataKey<KEY>,
        RECOVER extends RecoveryState,
        VAL
        >
        implements RestoreAnalyticsStateUseCase {

    ReadAnalyticsStatePort<KEY,RECOVER> reader;


    List<Interval> intervalList = List.of(
            Interval.M1,
            Interval.M3,
            Interval.M5,
            Interval.M15,
            Interval.M30,
            Interval.M60,
            Interval.M240
    );

    @Override
    public void restore(List<Integer> partitionIds) {
        for (Integer partitionId : partitionIds) {
            for (Interval interval : intervalList) {
                Map<KEY,RECOVER> map = reader.read(partitionId,interval);

            }
        }

    }

    protected abstract VAL trance(KEY key, RECOVER recoveryState);


}
