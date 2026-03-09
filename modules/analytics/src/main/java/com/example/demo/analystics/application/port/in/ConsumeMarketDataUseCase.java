package com.example.demo.analystics.application.port.in;

public interface ConsumeMarketDataUseCase<KEY,VAL> {
    void process(int partitionId,KEY key,VAL val);
}
