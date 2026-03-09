package com.example.demo.market_data.infrastructure.cache.meta;

import com.example.demo.market_data.domain.domain.snapshot.ExchangeSnapShotVal;
import com.example.demo.market_data.infrastructure.cache.MarketDataCache;
import org.springframework.stereotype.Component;

@Component
public class ExchangeCache extends MarketDataCache<Long ,ExchangeSnapShotVal>{
}
