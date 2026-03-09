package com.example.demo.market_data.infrastructure.cache.market;

import com.example.demo.market_data.domain.domain.Tick;
import com.example.demo.market_data.infrastructure.cache.MarketDataCache;
import org.springframework.stereotype.Component;

@Component
public class TickCache extends MarketDataCache<Long, Tick> {
}
