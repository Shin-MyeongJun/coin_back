package com.example.demo.analystics.domain.factory.indicator.buffer;

import com.example.demo.analystics.domain.buffer.indicator.IndicatorBuffer;
import com.example.demo.analystics.domain.buffer.indicator.PremiumIndicatorBuffer;
import com.example.demo.analystics.domain.domain.indicator.open.PremiumIndicator;
import com.example.demo.analystics.domain.factory.indicator.value.IndicatorFactory;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import org.springframework.stereotype.Component;

@Component
public class PremiumIndicatorBufferFactory extends IndicatorBufferFactory<PremiumKey,PremiumIndicator> {
    public PremiumIndicatorBufferFactory(IndicatorFactory<PremiumKey,PremiumIndicator> factory) {
        super(factory);
    }

    @Override
    public IndicatorBuffer<PremiumKey, PremiumIndicator> create() {
        return new PremiumIndicatorBuffer(getFactory());
    }
}
