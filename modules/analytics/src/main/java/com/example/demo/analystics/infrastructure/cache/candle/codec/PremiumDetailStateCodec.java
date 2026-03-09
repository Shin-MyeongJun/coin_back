package com.example.demo.analystics.infrastructure.cache.candle.codec;

import com.example.demo.analystics.domain.domain.candle.open.PremiumDetailCandle;
import com.example.demo.analystics.domain.domain.candle.value.OhlcData;
import com.example.demo.analystics.domain.domain.candle.value.PremiumDetailValue;
import com.example.demo.analystics.domain.domain.recovery.RecoveryCandleState;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Component
public class PremiumDetailStateCodec implements CandleStateCodec<PremiumDetailCandle, PremiumDetailValue> {

    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final char SEP = '|';

    @Override
    public byte[] encode(PremiumDetailCandle c) {
        var o = c.getOpen();
        var h = c.getHigh();
        var l = c.getLow();
        var cl = c.getClose();

        StringBuilder sb = new StringBuilder(256);

        sb.append(c.getOpenTimestamp()).append(SEP)
                .append(o.baseVal().toPlainString()).append(SEP)
                .append(o.compareVal().toPlainString()).append(SEP)
                .append(o.baseQuoteVal().toPlainString()).append(SEP)
                .append(o.compareQuoteVal().toPlainString()).append(SEP)

                .append(h.baseVal().toPlainString()).append(SEP)
                .append(h.compareVal().toPlainString()).append(SEP)
                .append(h.baseQuoteVal().toPlainString()).append(SEP)
                .append(h.compareQuoteVal().toPlainString()).append(SEP)

                .append(l.baseVal().toPlainString()).append(SEP)
                .append(l.compareVal().toPlainString()).append(SEP)
                .append(l.baseQuoteVal().toPlainString()).append(SEP)
                .append(l.compareQuoteVal().toPlainString()).append(SEP)

                .append(cl.baseVal().toPlainString()).append(SEP)
                .append(cl.compareVal().toPlainString()).append(SEP)
                .append(cl.baseQuoteVal().toPlainString()).append(SEP)
                .append(cl.compareQuoteVal().toPlainString());

        return sb.toString().getBytes(UTF8);
    }



    @Override
    public RecoveryCandleState<PremiumDetailValue> decode(byte[] bytes) {
        String str = new String(bytes, UTF8);

        // 💡 Cursor 역할을 할 배열 (Java는 Call by Reference가 안되므로 배열 꼼수 사용)
        // cursor[0] : 현재 읽고 있는 문자열의 시작 위치
        int[] cursor = {0};

        // 1. Timestamp (첫 번째 값)
        long timestamp = nextLong(str, cursor);

        // 2. Open (4개 값 순서대로 읽기)
        PremiumDetailValue open = new PremiumDetailValue(
                nextBigDecimal(str, cursor),
                nextBigDecimal(str, cursor),
                nextBigDecimal(str, cursor),
                nextBigDecimal(str, cursor)
        );

        // 3. High
        PremiumDetailValue high = new PremiumDetailValue(
                nextBigDecimal(str, cursor),
                nextBigDecimal(str, cursor),
                nextBigDecimal(str, cursor),
                nextBigDecimal(str, cursor)
        );

        // 4. Low
        PremiumDetailValue low = new PremiumDetailValue(
                nextBigDecimal(str, cursor),
                nextBigDecimal(str, cursor),
                nextBigDecimal(str, cursor),
                nextBigDecimal(str, cursor)
        );

        // 5. Close
        PremiumDetailValue close = new PremiumDetailValue(
                nextBigDecimal(str, cursor),
                nextBigDecimal(str, cursor),
                nextBigDecimal(str, cursor),
                nextBigDecimal(str, cursor)
        );

        OhlcData<PremiumDetailValue> ohlc = new OhlcData<>(open, high, low, close);

        return new RecoveryCandleState<>(ohlc, timestamp);
    }


    private BigDecimal nextBigDecimal(String str, int[] cursor) {
        String token = nextToken(str, cursor);
        return new BigDecimal(token);
    }

    private long nextLong(String str, int[] cursor) {
        String token = nextToken(str, cursor);
        return Long.parseLong(token);
    }

    private String nextToken(String str, int[] cursor) {
        int start = cursor[0];
        int end = str.indexOf(SEP, start);

        if (end == -1) {
            cursor[0] = str.length();
            return str.substring(start);
        } else {
            cursor[0] = end + 1;
            return str.substring(start, end);
        }
    }
}
