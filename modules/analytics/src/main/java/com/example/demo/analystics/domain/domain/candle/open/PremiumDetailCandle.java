package com.example.demo.analystics.domain.domain.candle.open;


import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.value.OhlcData;
import com.example.demo.analystics.domain.domain.candle.value.PremiumDetailValue;
import com.example.demo.analystics.domain.domain.key.PremiumKey;

public class PremiumDetailCandle extends OpenCandle<PremiumKey, PremiumDetailValue> {
    public PremiumDetailCandle(PremiumKey key, PremiumDetailValue val, Interval interval) {
        super(key, val,interval);
    }
    public PremiumDetailCandle(PremiumKey key, OhlcData<PremiumDetailValue> ohlcData, Interval interval, long timestamp) {
        super(key, ohlcData,interval,timestamp);
    }

    public long getBaseExchangeId(){
        return getKey().baseExchangeId();
    }
    public long getCompareExchangeId(){
        return getKey().compareExchangeId();
    }
    public String getBase(){
        return getKey().base();
    }
}
