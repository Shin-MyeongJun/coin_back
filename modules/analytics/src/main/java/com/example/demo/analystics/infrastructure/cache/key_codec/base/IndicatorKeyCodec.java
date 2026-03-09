package com.example.demo.analystics.infrastructure.cache.key_codec.base;

import com.example.demo.analystics.domain.domain.key.DataKey;
import com.example.demo.analystics.domain.domain.key.IndicatorKey;
import com.example.demo.analystics.domain.domain.key.IndicatorPriceDataKey;

public interface IndicatorKeyCodec<KEY extends DataKey<KEY>> {
    public byte[] encode (IndicatorPriceDataKey<KEY> key);
    public byte[] encode (KEY key, IndicatorKey indicatorKey);
    public IndicatorPriceDataKey<KEY> decode(byte[] bytes);
}
