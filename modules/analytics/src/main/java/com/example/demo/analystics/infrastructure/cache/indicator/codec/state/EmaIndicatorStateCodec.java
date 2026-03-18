package com.example.demo.analystics.infrastructure.cache.indicator.codec.state;

import com.example.demo.analystics.domain.domain.indicator.TradeIndicatorType;
import com.example.demo.analystics.domain.domain.indicator.open.state.EmaState;
import com.example.demo.analystics.domain.domain.recovery.RecoveryIndicatorState;
import com.example.demo.analystics.infrastructure.cache.indicator.codec.base.IndicatorStateCodec;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;


public class EmaIndicatorStateCodec implements IndicatorStateCodec<EmaState> {
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final char SEP = '|';

    @Override
    public byte[] encode(EmaState state,long timestamp) {
        if (state == null || state.prev() == null) {
            return new byte[0];
        }
        String buffer = String.valueOf(state.prev()) + SEP +
                timestamp;
        return buffer.getBytes(UTF8);
    }

    @Override
    public RecoveryIndicatorState decode(byte[] bytes) {
        String str = new String(bytes, UTF8);

        int p1 = str.indexOf(SEP);

        BigDecimal prev = new BigDecimal(str.substring(0, p1));
        long timestamp = Long.parseLong(str.substring(p1 + 1));

        EmaState  state = new EmaState(
                prev
        );
        return new RecoveryIndicatorState(
                state,
                timestamp
        );
    }

    @Override
    public Set<TradeIndicatorType> supportedTradeIndicatorTypes() {
        return Set.of(TradeIndicatorType.EMA);
    }


}
