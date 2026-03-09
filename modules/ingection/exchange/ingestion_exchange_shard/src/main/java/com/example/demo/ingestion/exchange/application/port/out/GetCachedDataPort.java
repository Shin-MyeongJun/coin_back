package com.example.demo.ingestion.exchange.application.port.out;

import com.example.demo.ingestion.exchange.domain.MarketCodeKey;
import com.example.demo.ingestion.exchange.domain.MarketCodeValue;

public interface GetCachedDataPort<KEY,VAL> {
    VAL get(KEY key);

    interface forMarketCode extends GetCachedDataPort<MarketCodeKey, MarketCodeValue> {} ;
}
