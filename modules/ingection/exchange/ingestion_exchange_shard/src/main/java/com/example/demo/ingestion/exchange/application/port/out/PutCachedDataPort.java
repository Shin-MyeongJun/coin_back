package com.example.demo.ingestion.exchange.application.port.out;

import com.example.demo.ingestion.exchange.domain.MarketCodeKey;
import com.example.demo.ingestion.exchange.domain.MarketCodeValue;

public interface PutCachedDataPort<KEY,VAL> {
    void put(KEY key, VAL val);
    interface forMarketCode extends PutCachedDataPort<MarketCodeKey, MarketCodeValue> {} ;
}
