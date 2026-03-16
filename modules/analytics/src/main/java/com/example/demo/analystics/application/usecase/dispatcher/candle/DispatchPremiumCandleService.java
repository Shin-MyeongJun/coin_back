package com.example.demo.analystics.application.usecase.dispatcher.candle;

import com.example.demo.analystics.application.usecase.base.DispatchingAnalyticsService;
import com.example.demo.analystics.domain.dispatch_manager.AnalyticsMangerController;
import com.example.demo.analystics.domain.domain.candle.open.PremiumCandle;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DispatchPremiumCandleService extends DispatchingAnalyticsService<PremiumKey, BigDecimal, PremiumCandle> {

    public DispatchPremiumCandleService(AnalyticsMangerController<PremiumKey, BigDecimal, PremiumCandle, ?> controller) {
        super(controller);
    }
}
