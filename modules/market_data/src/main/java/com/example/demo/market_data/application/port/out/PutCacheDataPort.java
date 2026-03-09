package com.example.demo.market_data.application.port.out;

public interface PutCacheDataPort<KEY,VAL> {
    public void put(KEY key, VAL val);
}
