package com.example.demo.analystics.infrastructure.cache.key_codec;


import com.example.demo.analystics.domain.domain.indicator.TradeIndicatorType;
import com.example.demo.analystics.domain.domain.key.IndicatorKey;
import com.example.demo.analystics.domain.domain.key.IndicatorPriceDataKey;
import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.analystics.infrastructure.cache.key_codec.base.IndicatorKeyCodec;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Component
public class TickIndicatorKeyCodec implements IndicatorKeyCodec<TickKey> {
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final char SEP = ':';

    @Override
    public byte[] encode(IndicatorPriceDataKey<TickKey> key)
    {
        return (key.dataKey().MarketCodeId() +SEP +
                key.indicatorKey().period() + SEP +
                key.indicatorKey().type().getName()).getBytes();
    }

    @Override
    public byte[] encode(TickKey key, IndicatorKey indicatorKey) {
        return  (key.MarketCodeId() +SEP +
                indicatorKey.period() + SEP +
                indicatorKey.type().getName()).getBytes();
    }

    @Override
    public IndicatorPriceDataKey<TickKey> decode(byte[] bytes) {
        String str = new String(bytes, UTF8);
        int p1 = str.indexOf(SEP);
        int p2 = str.indexOf(SEP, p1 + 1);
        Long id = Long.parseLong(str.substring(0, p1));
        int period = Integer.parseInt(str.substring(p1 + 1, p2));
        TradeIndicatorType type = TradeIndicatorType.valueOf(str.substring(p2 + 1));
        TickKey tickKey = new TickKey(id);
        IndicatorKey indicatorKey = new IndicatorKey(period,type);
        return new IndicatorPriceDataKey<>(
                tickKey,
                indicatorKey
        );
    }

}
