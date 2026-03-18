package com.example.demo.analystics.infrastructure.cache.indicator.codec.state;

import com.example.demo.analystics.domain.domain.indicator.TradeIndicatorType;
import com.example.demo.analystics.domain.domain.indicator.open.state.TrState;
import com.example.demo.analystics.domain.domain.recovery.RecoveryIndicatorState;
import com.example.demo.analystics.infrastructure.cache.indicator.codec.base.IndicatorStateCodec;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class TrIndicatorStateCodec implements IndicatorStateCodec<TrState> {
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final char SEP = '|';


    @Override
    public byte[] encode(TrState state,long timestamp) {
        String buffer = String.valueOf(state.high()) + SEP +
                state.low() + SEP +
                state.prevClose() + SEP +
                timestamp;
        return buffer.getBytes(UTF8);
    }

    @Override
    public RecoveryIndicatorState decode(byte[] bytes) {
        String str = new String(bytes, UTF8);
        int p1 = str.indexOf(SEP);
        int p2 = str.indexOf(SEP, p1 + 1);
        int p3 = str.indexOf(SEP, p2 + 1);

        BigDecimal high = new BigDecimal(str.substring(0, p1));
        BigDecimal low = new BigDecimal(str.substring(p1 + 1, p3));
        BigDecimal prevClose = new BigDecimal(str.substring(p2 + 1, p3));
        long timestamp = Long.parseLong(str.substring(p3 + 1));
        TrState state = new TrState(high,low,prevClose);
        return new RecoveryIndicatorState(
                state,
                timestamp
        );
    }

    @Override
    public Set<TradeIndicatorType> supportedTradeIndicatorTypes() {
        return Set.of(TradeIndicatorType.TR);
    }
}
