package com.example.demo.analystics.domain.buffer.indicator;


import com.example.demo.analystics.domain.domain.indicator.open.PremiumIndicator;
import com.example.demo.analystics.domain.factory.indicator.value.IndicatorFactory;
import com.example.demo.analystics.domain.domain.key.PremiumKey;

public class PremiumIndicatorBuffer extends IndicatorBuffer<PremiumKey, PremiumIndicator> {

    public PremiumIndicatorBuffer(IndicatorFactory<PremiumKey,PremiumIndicator> factory) {
        super(factory);
    }
}
