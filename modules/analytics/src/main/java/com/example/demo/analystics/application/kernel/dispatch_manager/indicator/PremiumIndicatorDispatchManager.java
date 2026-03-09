package com.example.demo.analystics.application.kernel.dispatch_manager.indicator;


import com.example.demo.analystics.application.kernel.base.DispatchIndicatorManager;
import com.example.demo.analystics.application.port.out.WriteAnalyticsValuePort;
import com.example.demo.analystics.domain.domain.indicator.close.PremiumCloseIndicator;
import com.example.demo.analystics.domain.domain.indicator.open.PremiumIndicator;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.factory.indicator.buffer.IndicatorBufferFactory;
import com.example.demo.analystics.domain.manager.indicator.IndicatorManager;
import com.example.demo.analystics.domain.manager.indicator.PremiumIndicatorManager;
import com.example.demo.analystics.domain.service.ClosingData;
import org.springframework.stereotype.Component;

@Component
public class PremiumIndicatorDispatchManager extends DispatchIndicatorManager<PremiumKey,
        PremiumIndicator,
        PremiumCloseIndicator,
        IndicatorManager<PremiumKey,PremiumIndicator,PremiumCloseIndicator>
        >
{

    private final IndicatorBufferFactory<PremiumKey, PremiumIndicator> factory;
    private final ClosingData<PremiumIndicator,PremiumCloseIndicator> closeService;

    protected PremiumIndicatorDispatchManager(WriteAnalyticsValuePort<PremiumCloseIndicator> dataSaveUseCase
            , IndicatorBufferFactory<PremiumKey, PremiumIndicator> factory
            , ClosingData<PremiumIndicator ,PremiumCloseIndicator> closeService) {
        super(dataSaveUseCase);
        this.factory = factory;
        this.closeService = closeService;
    }


    @Override
    protected PremiumIndicatorManager createManager() {
        return new PremiumIndicatorManager(factory,closeService);
    }
}
