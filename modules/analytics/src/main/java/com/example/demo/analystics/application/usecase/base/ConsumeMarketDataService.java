package com.example.demo.analystics.application.usecase.base;

import com.example.demo.analystics.application.port.in.ConsumeMarketDataUseCase;
import com.example.demo.analystics.application.port.in.DispatchingDataUseCase;
import com.example.demo.analystics.domain.domain.key.DataKey;

import java.util.List;

public abstract class ConsumeMarketDataService<
         KEY extends DataKey<KEY>
        ,VAL extends Comparable<VAL>> implements ConsumeMarketDataUseCase<KEY,VAL> {

    private final List<DispatchingDataUseCase<KEY,VAL>> dispatchers;

    protected ConsumeMarketDataService(List<DispatchingDataUseCase<KEY,VAL>>dispatchers) {
        this.dispatchers = dispatchers;
    }

    @Override
    public void process(int partitionId,KEY key,VAL val) {
        dispatchers.forEach(dispatcher -> dispatcher.dispatch(partitionId, key, val));
    }
}
