package com.example.demo.analystics.domain.dispatch_manager.indicator;


import com.example.demo.analystics.domain.dispatch_manager.IndicatorMangerController;
import com.example.demo.analystics.domain.domain.indicator.close.PremiumCloseIndicator;
import com.example.demo.analystics.domain.domain.indicator.open.PremiumIndicator;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.factory.indicator.buffer.IndicatorBufferFactory;
import com.example.demo.analystics.domain.manager.indicator.IndicatorManager;
import com.example.demo.analystics.domain.manager.indicator.PremiumIndicatorManager;
import com.example.demo.analystics.domain.service.ClosingData;
import org.springframework.stereotype.Component;

@Component
public class PremiumIndicatorDispatchManager extends IndicatorMangerController<PremiumKey,
        PremiumIndicator,
        PremiumCloseIndicator,
        IndicatorManager<PremiumKey,PremiumIndicator,PremiumCloseIndicator>
        >
{

    private final ClosingData<PremiumIndicator,PremiumCloseIndicator> closingData;
    private final IndicatorBufferFactory<PremiumKey,PremiumIndicator> bufferFactory;

    public PremiumIndicatorDispatchManager(ClosingData<PremiumIndicator, PremiumCloseIndicator> closingData, IndicatorBufferFactory<PremiumKey, PremiumIndicator> bufferFactory) {
        this.closingData = closingData;
        this.bufferFactory = bufferFactory;
    }


    @Override
    protected IndicatorManager<PremiumKey, PremiumIndicator, PremiumCloseIndicator> createManager() {
        return new PremiumIndicatorManager(bufferFactory,closingData);
    }
}
