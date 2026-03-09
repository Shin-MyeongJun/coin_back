package com.example.demo.market_data.infrastructure.cache.market;

import com.example.demo.market_data.domain.domain.Fx;
import com.example.demo.market_data.domain.domain.FxKey;
import com.example.demo.market_data.infrastructure.cache.MarketDataCache;
import org.springframework.stereotype.Component;

@Component
public class FxCache extends MarketDataCache<FxKey,Fx> {
}
