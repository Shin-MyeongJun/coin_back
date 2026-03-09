package com.example.demo.analystics.domain.manager.indicator;

import com.example.demo.analystics.domain.domain.indicator.close.PremiumCloseIndicator;
import com.example.demo.analystics.domain.domain.indicator.open.PremiumIndicator;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.factory.indicator.buffer.IndicatorBufferFactory;
import com.example.demo.analystics.domain.service.ClosingData;


public class PremiumIndicatorManager  extends IndicatorManager<PremiumKey ,PremiumIndicator ,PremiumCloseIndicator> {

    public PremiumIndicatorManager(IndicatorBufferFactory<PremiumKey,PremiumIndicator> factory,
                                   ClosingData<PremiumIndicator, PremiumCloseIndicator> closeService) {
        super(factory,closeService);
    }
}
