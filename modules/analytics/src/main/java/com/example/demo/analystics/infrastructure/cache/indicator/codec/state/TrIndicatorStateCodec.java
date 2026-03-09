package com.example.demo.analystics.infrastructure.cache.indicator.codec.state;

import com.example.demo.analystics.domain.domain.indicator.TradeIndicatorType;
import com.example.demo.analystics.domain.domain.indicator.open.state.TrState;
import com.example.demo.analystics.domain.domain.recovery.RecoveryIndicatorState;
import com.example.demo.analystics.infrastructure.cache.indicator.codec.base.IndicatorStateCodec;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class TrIndicatorStateCodec implements IndicatorStateCodec<TrState> {
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final char SEP = '|';


    @Override
    public byte[] encode(TrState state) {
        return new byte[0];
    }

    @Override
    public RecoveryIndicatorState<TrState> decode(byte[] bytes) {
        return null;
    }

    @Override
    public Set<TradeIndicatorType> supportedTradeIndicatorTypes() {
        return Set.of(TradeIndicatorType.TR);
    }
}
