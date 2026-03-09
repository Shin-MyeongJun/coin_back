package com.example.demo.analystics.infrastructure.cache.indicator.codec.state;

import com.example.demo.analystics.domain.domain.indicator.TradeIndicatorType;
import com.example.demo.analystics.domain.domain.indicator.open.state.EmaState;
import com.example.demo.analystics.domain.domain.recovery.RecoveryIndicatorState;
import com.example.demo.analystics.infrastructure.cache.indicator.codec.base.IndicatorStateCodec;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;


public class EmaIndicatorStateCodec implements IndicatorStateCodec<EmaState> {
    private static final Charset UTF8 = StandardCharsets.UTF_8;

    @Override
    public byte[] encode(EmaState state) {
        if (state == null || state.prev() == null) {
            return new byte[0];
        }
        return state.prev().toPlainString().getBytes(UTF8);
    }

    @Override
    public RecoveryIndicatorState decode(byte[] bytes) {
        return null;
    }

    @Override
    public Set<TradeIndicatorType> supportedTradeIndicatorTypes() {
        return Set.of(TradeIndicatorType.EMA);
    }


}
