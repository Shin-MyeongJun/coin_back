package com.example.demo.ingestion.exchange.infrastruct.cache;

import com.example.demo.ingestion.exchange.application.port.out.GetCachedDataPort;
import com.example.demo.ingestion.exchange.application.port.out.PutCachedDataPort;
import com.example.demo.ingestion.exchange.domain.MarketCodeKey;
import com.example.demo.ingestion.exchange.domain.MarketCodeValue;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MarketCodeValueCache implements GetCachedDataPort.forMarketCode , PutCachedDataPort.forMarketCode {
    private final Map<MarketCodeKey, MarketCodeValue> marketCodeValueMap
            = new ConcurrentHashMap<>();

    public MarketCodeValue get(MarketCodeKey marketCodeKey) {
        return marketCodeValueMap.get(marketCodeKey);
    }

    public void put(MarketCodeKey marketCodeKey, MarketCodeValue marketCodeValue) {
        marketCodeValueMap.put(marketCodeKey, marketCodeValue);
    }
}
