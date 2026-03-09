package com.example.demo.analystics.infrastructure.cache.key_codec;


import com.example.demo.analystics.domain.domain.indicator.TradeIndicatorType;
import com.example.demo.analystics.domain.domain.key.IndicatorKey;
import com.example.demo.analystics.domain.domain.key.IndicatorPriceDataKey;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.infrastructure.cache.key_codec.base.IndicatorKeyCodec;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class PremiumIndicatorCodec implements IndicatorKeyCodec<PremiumKey> {
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final char SEP = ':';

    @Override
    public byte[] encode(IndicatorPriceDataKey<PremiumKey> key)
    {
        return (key.dataKey().base()+ SEP+
                key.dataKey().baseExchangeId() + SEP +
                key.dataKey().compareExchangeId() + SEP +
                key.indicatorKey().period() + SEP +
                key.indicatorKey().type().getName())
                .getBytes();
    }

    @Override
    public byte[] encode(PremiumKey key, IndicatorKey indicatorKey) {
        return (key.base()+ SEP+
                key.baseExchangeId() + SEP +
                key.compareExchangeId() + SEP +
                indicatorKey.period() + SEP +
                indicatorKey.type().getName())
                .getBytes();
    }

    @Override
    public IndicatorPriceDataKey<PremiumKey> decode(byte[] bytes) {
        String str = new String(bytes, UTF8);
        int p1 = str.indexOf(SEP);
        int p2 = str.indexOf(SEP, p1 + 1);
        int p3 = str.indexOf(SEP, p2 + 1);
        int p4 = str.indexOf(SEP, p3 + 1);
        String base = str.substring(0, p1);
        Long baseId = Long.parseLong(str.substring(p1+1, p2));
        Long compareId = Long.parseLong(str.substring(p2+1, p3));
        int period = Integer.parseInt(str.substring(p3 + 1, p4));
        TradeIndicatorType type = TradeIndicatorType.valueOf(str.substring(p4+1));
        PremiumKey pk = new PremiumKey(base, baseId, compareId);
        IndicatorKey ink = new IndicatorKey(period, type);
        return new IndicatorPriceDataKey<>(
                pk,
                ink
        );
    }
}
