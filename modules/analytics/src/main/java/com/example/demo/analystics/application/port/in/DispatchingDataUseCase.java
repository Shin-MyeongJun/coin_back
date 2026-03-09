package com.example.demo.analystics.application.port.in;

public interface DispatchingDataUseCase<KEY,VAL> {
    void dispatch(int partitionId , KEY key, VAL val);
}
