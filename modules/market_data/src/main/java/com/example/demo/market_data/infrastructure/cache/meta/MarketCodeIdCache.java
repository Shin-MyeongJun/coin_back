package com.example.demo.market_data.infrastructure.cache.meta;

import com.example.demo.market_data.domain.domain.snapshot.MarketCodeSnapShotVal;
import com.example.demo.market_data.infrastructure.cache.MarketDataCache;
import org.springframework.stereotype.Component;

@Component
public class MarketCodeIdCache extends MarketDataCache<MarketCodeSnapShotVal,Long> {
}
