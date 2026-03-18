package com.example.demo.analystics.infrastructure.cache.indicator.codec.state;

import com.example.demo.analystics.domain.domain.indicator.TradeIndicatorType;
import com.example.demo.analystics.domain.domain.indicator.open.state.WelfordState;
import com.example.demo.analystics.domain.domain.recovery.RecoveryIndicatorState;
import com.example.demo.analystics.infrastructure.cache.indicator.codec.base.IndicatorStateCodec;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class WelfordIndicaotrCodec implements IndicatorStateCodec<WelfordState> {
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final char SEP = '|';

    @Override
    public byte[] encode(WelfordState state,long timestamp) {
        String buffer = String.valueOf(state.mean()) + SEP +
                state.count() + SEP +
                timestamp;
        return buffer.getBytes(UTF8);
    }

    @Override
    public RecoveryIndicatorState decode(byte[] bytes) {
        String str = new String(bytes, UTF8);
        int p1 = str.indexOf(SEP);
        int p2 = str.indexOf(SEP, p1 + 1);

        BigDecimal mean = new BigDecimal(str.substring(0, p1));
        BigDecimal count = new BigDecimal(str.substring(p1 + 1, p2));
        long timestamp = Long.parseLong(str.substring(p2 + 1));

        WelfordState state = new WelfordState(mean, count);
        return new RecoveryIndicatorState(state, timestamp);
    }

    @Override
    public Set<TradeIndicatorType> supportedTradeIndicatorTypes() {
        return Set.of(TradeIndicatorType.MEAN,TradeIndicatorType.STDDEV);
    }
}
