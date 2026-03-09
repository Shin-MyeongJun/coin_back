package com.example.demo.analystics.domain.factory.indicator.buffer;

import com.example.demo.analystics.domain.buffer.indicator.IndicatorBuffer;
import com.example.demo.analystics.domain.domain.indicator.open.OpenTradeIndicator;
import com.example.demo.analystics.domain.factory.indicator.value.IndicatorFactory;
import com.example.demo.analystics.domain.domain.key.DataKey;

public abstract class IndicatorBufferFactory<
        KEY extends DataKey<KEY>,
        IND extends OpenTradeIndicator<KEY>> {
    private final IndicatorFactory<KEY,IND> factory;

    public IndicatorBufferFactory(IndicatorFactory<KEY,IND> factory) {
        this.factory = factory;
    }

    protected  IndicatorFactory<KEY,IND> getFactory() {
        return factory;
    }

    public abstract IndicatorBuffer<KEY,IND> create();
}
