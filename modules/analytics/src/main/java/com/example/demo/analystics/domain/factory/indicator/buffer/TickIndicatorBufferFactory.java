package com.example.demo.analystics.domain.factory.indicator.buffer;

import com.example.demo.analystics.domain.buffer.indicator.IndicatorBuffer;
import com.example.demo.analystics.domain.buffer.indicator.TickIndicatorBuffer;
import com.example.demo.analystics.domain.domain.indicator.open.TickIndicator;
import com.example.demo.analystics.domain.factory.indicator.value.IndicatorFactory;
import com.example.demo.analystics.domain.domain.key.TickKey;
import org.springframework.stereotype.Component;

@Component
public class TickIndicatorBufferFactory extends IndicatorBufferFactory<TickKey, TickIndicator> {
    public TickIndicatorBufferFactory(IndicatorFactory<TickKey, TickIndicator> factory) {
        super(factory);
    }

    @Override
    public IndicatorBuffer<TickKey, TickIndicator> create() {
        return new TickIndicatorBuffer(getFactory());
    }
}
