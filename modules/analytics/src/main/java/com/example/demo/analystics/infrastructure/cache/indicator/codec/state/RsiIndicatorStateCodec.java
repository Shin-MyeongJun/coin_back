package com.example.demo.analystics.infrastructure.cache.indicator.codec.state;

import com.example.demo.analystics.domain.domain.indicator.TradeIndicatorType;
import com.example.demo.analystics.domain.domain.indicator.open.state.RsiState;
import com.example.demo.analystics.domain.domain.recovery.RecoveryIndicatorState;
import com.example.demo.analystics.infrastructure.cache.indicator.codec.base.IndicatorStateCodec;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class RsiIndicatorStateCodec implements IndicatorStateCodec<RsiState> {
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final char SEP = '|';

    @Override
    public byte[] encode(RsiState state,long timestamp) {
        String buffer = String.valueOf(state.avgGainScaled()) + SEP +
                state.avgLossScaled() + SEP +
                state.prevCloseScaled() + SEP +
                state.initCount() + SEP +
                timestamp;
        return buffer.getBytes(UTF8);
    }

    @Override
    public RecoveryIndicatorState decode(byte[] bytes) {
        String str = new String(bytes, UTF8);

        int p1 = str.indexOf(SEP);
        int p2 = str.indexOf(SEP, p1 + 1);
        int p3 = str.indexOf(SEP, p2 + 1);
        int p4 = str.indexOf(SEP, p3 + 1);

        BigDecimal avgGainScaled = new BigDecimal(str.substring(0, p1));
        BigDecimal avgLossScaled = new BigDecimal(str.substring(p1 + 1, p2));
        BigDecimal prevCloseScaled = new BigDecimal(str.substring(p2+1,p3));
        int initCount = Integer.parseInt(str.substring(p3 + 1,p4));
        long timestamp = Long.parseLong(str.substring(p4 + 1));
        RsiState state = new RsiState(
                avgGainScaled,
                avgLossScaled,
                prevCloseScaled,
                initCount
        );

        return new RecoveryIndicatorState(
                state,
                timestamp
        );
    }

    @Override
    public Set<TradeIndicatorType> supportedTradeIndicatorTypes() {
        return Set.of(TradeIndicatorType.RSI);
    }
}
