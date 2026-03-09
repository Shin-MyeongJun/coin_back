package com.example.demo.analystics.domain.manager.indicator;

import com.example.demo.analystics.domain.domain.indicator.close.TickCloseIndicator;
import com.example.demo.analystics.domain.domain.indicator.open.TickIndicator;
import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.analystics.domain.factory.indicator.buffer.IndicatorBufferFactory;
import com.example.demo.analystics.domain.service.ClosingData;

public class TickIndicatorManager extends IndicatorManager<TickKey, TickIndicator, TickCloseIndicator> {

    public TickIndicatorManager(IndicatorBufferFactory<TickKey, TickIndicator> factory
            , ClosingData<TickIndicator, TickCloseIndicator> closeService) {
        super(factory,closeService);
    }
}
