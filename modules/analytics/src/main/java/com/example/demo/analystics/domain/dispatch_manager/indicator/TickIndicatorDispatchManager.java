package com.example.demo.analystics.domain.dispatch_manager.indicator;

import com.example.demo.analystics.domain.dispatch_manager.IndicatorMangerController;
import com.example.demo.analystics.domain.domain.indicator.close.TickCloseIndicator;
import com.example.demo.analystics.domain.domain.indicator.open.TickIndicator;
import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.analystics.domain.factory.indicator.buffer.IndicatorBufferFactory;
import com.example.demo.analystics.domain.manager.indicator.TickIndicatorManager;
import com.example.demo.analystics.domain.service.ClosingData;
import org.springframework.stereotype.Component;

@Component
public class TickIndicatorDispatchManager  extends IndicatorMangerController<TickKey, TickIndicator,TickCloseIndicator,TickIndicatorManager> {

    private final IndicatorBufferFactory<TickKey,TickIndicator> factory;
    private final ClosingData<TickIndicator, TickCloseIndicator> closeService;
    protected TickIndicatorDispatchManager
            (
             IndicatorBufferFactory<TickKey,TickIndicator> factory,
             ClosingData<TickIndicator, TickCloseIndicator> closeService) {
        this.factory = factory;
        this.closeService = closeService;
    }


    @Override
    protected TickIndicatorManager createManager() {
        return new TickIndicatorManager(factory,closeService);
    }
}

