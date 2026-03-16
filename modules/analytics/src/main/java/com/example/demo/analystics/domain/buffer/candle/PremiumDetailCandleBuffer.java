package com.example.demo.analystics.domain.buffer.candle;

import com.example.demo.analystics.domain.domain.candle.open.PremiumDetailCandle;
import com.example.demo.analystics.domain.domain.candle.value.PremiumDetailValue;
import com.example.demo.analystics.domain.domain.key.PremiumKey;




public class PremiumDetailCandleBuffer extends CandleBuffer<PremiumKey, PremiumDetailValue, PremiumDetailCandle> {


    @Override
    protected PremiumDetailCandle createCandle(PremiumKey key, PremiumDetailValue premiumDetailValue) {
        return new PremiumDetailCandle(key, premiumDetailValue);
    }
}
