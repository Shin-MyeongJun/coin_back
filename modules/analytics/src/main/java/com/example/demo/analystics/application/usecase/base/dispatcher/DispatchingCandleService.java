package com.example.demo.analystics.application.usecase.base.dispatcher;

import com.example.demo.analystics.application.kernel.base.CandleManagerController;
import com.example.demo.analystics.application.port.in.DispatchingDataUseCase;
import com.example.demo.analystics.domain.domain.key.DataKey;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class DispatchingCandleService<KEY extends DataKey<KEY>,VAL extends Comparable<VAL>> implements DispatchingDataUseCase<KEY,VAL> {
    private final CandleManagerController<KEY,VAL,?,?,?,?> controller;
    @Override
    public void dispatch(int partitionId , KEY key, VAL val){
        controller.insert(partitionId, key, val);
    }

}
