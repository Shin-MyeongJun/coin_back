package com.example.demo.analystics.infrastructure.cache.candle.codec;

import com.example.demo.analystics.domain.domain.candle.open.OpenCandle;
import com.example.demo.analystics.domain.domain.recovery.RecoveryCandleState;

public interface CandleStateCodec <CANDLE extends OpenCandle<?,VAL>,VAL extends Comparable<VAL>> {
    public byte[] encode(CANDLE c);
    public RecoveryCandleState<VAL> decode(byte[] bytes);
}
