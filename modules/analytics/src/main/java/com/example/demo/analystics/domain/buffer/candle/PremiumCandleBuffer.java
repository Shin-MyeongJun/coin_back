package com.example.demo.analystics.domain.buffer.candle;


import com.example.demo.analystics.domain.domain.candle.open.PremiumCandle;
import com.example.demo.analystics.domain.domain.key.PremiumKey;

import java.math.BigDecimal;


public class PremiumCandleBuffer extends CandleBuffer<PremiumKey, BigDecimal, PremiumCandle> {


    @Override
    protected PremiumCandle createCandle(PremiumKey key, BigDecimal bigDecimal) {
        return new PremiumCandle(key, bigDecimal);
    }
}
