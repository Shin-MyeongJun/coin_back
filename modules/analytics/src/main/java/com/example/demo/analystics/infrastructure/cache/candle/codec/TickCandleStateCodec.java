package com.example.demo.analystics.infrastructure.cache.candle.codec;

import com.example.demo.analystics.domain.domain.candle.open.TickCandle;
import com.example.demo.analystics.domain.domain.candle.value.OhlcData;
import com.example.demo.analystics.domain.domain.recovery.RecoveryCandleState;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Component
public class TickCandleStateCodec implements CandleStateCodec<TickCandle, BigDecimal> {

    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final char SEP = '|';

    @Override
    public byte[] encode(TickCandle c) {
        StringBuilder sb = new StringBuilder(256);
        sb.append(c.getOpenTimestamp()).append(SEP)
                .append(c.getOpen()).append(SEP)
                .append(c.getHigh()).append(SEP)
                .append(c.getLow()).append(SEP)
                .append(c.getClose()).append(SEP);
        return sb.toString().getBytes(UTF8);
    }

    @Override
    public RecoveryCandleState<BigDecimal> decode(byte[] bytes) {
        String str = new String(bytes, UTF8);

        int p1 = str.indexOf(SEP);
        int p2 = str.indexOf(SEP, p1 + 1);
        int p3 = str.indexOf(SEP, p2 + 1);
        int p4 = str.indexOf(SEP, p3 + 1);

        long timestamp = Long.parseLong(str.substring(0, p1));
        BigDecimal open = new BigDecimal(str.substring(p1 + 1, p2));
        BigDecimal high = new BigDecimal(str.substring(p2 + 1, p3));
        BigDecimal low = new BigDecimal(str.substring(p3 + 1, p4));
        BigDecimal close = new BigDecimal(str.substring(p4 + 1)); // 마지막 값

        OhlcData<BigDecimal> ohlc = new OhlcData<>(open, high, low, close);

        return new RecoveryCandleState<>(ohlc, timestamp);
    }


}
