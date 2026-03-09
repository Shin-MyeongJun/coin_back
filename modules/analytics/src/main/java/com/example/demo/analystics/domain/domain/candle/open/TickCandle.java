package com.example.demo.analystics.domain.domain.candle.open;


import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.value.OhlcData;
import com.example.demo.analystics.domain.domain.key.TickKey;

import java.math.BigDecimal;

public class TickCandle extends OpenCandle<TickKey, BigDecimal> {
    public TickCandle(TickKey key, BigDecimal val, Interval interval) {
        super(key, val,interval);
    }
    public TickCandle(TickKey key, OhlcData<BigDecimal> ohlcData,Interval interval, long timestamp) {
        super(key,ohlcData ,interval, timestamp);
    }
    public Long getMarketCodeId(){
        return getKey().MarketCodeId();
    }
}
