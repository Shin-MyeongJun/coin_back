package com.example.demo.market_data.application.port.out;

import java.util.List;

public interface WriteRedisLatestDataPort<VAL> {
    void upsert(VAL val);
    void upsertAll(List<VAL> vals);
}
