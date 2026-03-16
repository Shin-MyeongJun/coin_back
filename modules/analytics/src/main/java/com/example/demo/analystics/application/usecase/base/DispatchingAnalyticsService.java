package com.example.demo.analystics.application.usecase.base;

import com.example.demo.analystics.application.port.in.DispatchingDataUseCase;
import com.example.demo.analystics.domain.dispatch_manager.AnalyticsMangerController;
import com.example.demo.analystics.domain.domain.key.DataKey;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class DispatchingAnalyticsService
        <
                KEY extends DataKey<KEY>,
                VAL extends Comparable<VAL>,
                TD
                >
        implements DispatchingDataUseCase<KEY,VAL> {
    private final AnalyticsMangerController<KEY,VAL,TD,?> controller;
    @Override
    public void dispatch(int partitionId , KEY key, VAL val){
        controller.insert(partitionId, key, val);
    }

}
