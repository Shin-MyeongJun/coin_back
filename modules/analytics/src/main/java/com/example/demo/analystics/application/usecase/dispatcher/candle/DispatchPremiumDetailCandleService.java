package com.example.demo.analystics.application.usecase.dispatcher.candle;

import com.example.demo.analystics.application.usecase.base.DispatchingAnalyticsService;
import com.example.demo.analystics.domain.dispatch_manager.AnalyticsMangerController;
import com.example.demo.analystics.domain.domain.candle.open.PremiumDetailCandle;
import com.example.demo.analystics.domain.domain.candle.value.PremiumDetailValue;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import org.springframework.stereotype.Component;

@Component
public class DispatchPremiumDetailCandleService extends DispatchingAnalyticsService<PremiumKey, PremiumDetailValue, PremiumDetailCandle> {

    public DispatchPremiumDetailCandleService(AnalyticsMangerController<PremiumKey, PremiumDetailValue, PremiumDetailCandle, ?> controller) {
        super(controller);
    }
}
