package com.example.demo.analystics.domain.domain.candle.open;


import com.example.demo.analystics.domain.domain.candle.value.OhlcData;
import com.example.demo.analystics.domain.domain.key.PremiumKey;

import java.math.BigDecimal;

public class PremiumCandle extends OpenCandle<PremiumKey, BigDecimal> {
    public PremiumCandle(PremiumKey key, BigDecimal val) {
        super(key,val);
    }
    public PremiumCandle(PremiumKey key, OhlcData<BigDecimal> ohlcData, long timestamp) {
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
