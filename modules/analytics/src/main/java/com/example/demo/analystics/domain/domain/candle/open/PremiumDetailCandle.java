package com.example.demo.analystics.domain.domain.candle.open;


import com.example.demo.analystics.domain.domain.candle.value.OhlcData;
import com.example.demo.analystics.domain.domain.candle.value.PremiumDetailValue;
import com.example.demo.analystics.domain.domain.key.PremiumKey;

public class PremiumDetailCandle extends OpenCandle<PremiumKey, PremiumDetailValue> {
    public PremiumDetailCandle(PremiumKey key, PremiumDetailValue val) {
        super(key, val);
    }
    public PremiumDetailCandle(PremiumKey key, OhlcData<PremiumDetailValue> ohlcData, long timestamp) {
        super(key, ohlcData,timestamp);
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
