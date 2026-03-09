package com.example.demo.market_data.infrastructure.cache;

import com.example.demo.market_data.application.port.out.GetCacheDataPort;
import com.example.demo.market_data.application.port.out.PutCacheDataPort;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public abstract class MarketDataCache<KEY ,VAL> implements GetCacheDataPort<KEY,VAL> , PutCacheDataPort<KEY,VAL> {
    private  final Map<KEY,VAL> cache = new ConcurrentHashMap<>();

    @Override
    public Optional<VAL> get(KEY key) {
        return Optional.ofNullable(cache.get(key));
    }

    @Override
    public void put(KEY key, VAL val) {
        cache.put(key, val);
    }
}
