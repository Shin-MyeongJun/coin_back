package com.example.demo.analystics.application.usecase.base.dispatcher;

import com.example.demo.analystics.application.kernel.base.DispatchIndicatorManager;
import com.example.demo.analystics.application.port.in.DispatchingDataUseCase;
import com.example.demo.analystics.domain.domain.key.DataKey;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
public abstract class DispatchingIndicatorService<KEY extends DataKey<KEY>> implements DispatchingDataUseCase<KEY, BigDecimal> {
    private  final DispatchIndicatorManager<KEY,?,?,?> core;

    @Override
    public void dispatch(int partitionId, KEY key, BigDecimal val) {
        core.dispatch(partitionId,key,val);
    }
}
