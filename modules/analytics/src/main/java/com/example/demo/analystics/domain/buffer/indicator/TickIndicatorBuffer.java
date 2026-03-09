package com.example.demo.analystics.domain.buffer.indicator;

import com.example.demo.analystics.domain.domain.indicator.open.TickIndicator;
import com.example.demo.analystics.domain.factory.indicator.value.IndicatorFactory;
import com.example.demo.analystics.domain.domain.key.TickKey;

public class TickIndicatorBuffer extends IndicatorBuffer<TickKey, TickIndicator> {

    public  TickIndicatorBuffer(IndicatorFactory<TickKey,TickIndicator> factory) {
        super(factory);
    }
}
