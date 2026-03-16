package com.example.demo.analystics.domain.buffer.candle;


import com.example.demo.analystics.domain.domain.candle.open.TickCandle;
import com.example.demo.analystics.domain.domain.key.TickKey;

import java.math.BigDecimal;


public class TickCandleBuffer extends CandleBuffer<TickKey, BigDecimal, TickCandle> {
    @Override
    protected TickCandle createCandle(TickKey key, BigDecimal bigDecimal) {
        return new TickCandle(key, bigDecimal);
    }
}
