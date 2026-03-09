package com.example.demo.market_data.application.port.out;

import java.util.Optional;

public interface GetCacheDataPort<KEY,VAL> {
    public Optional<VAL> get(KEY key);
}
