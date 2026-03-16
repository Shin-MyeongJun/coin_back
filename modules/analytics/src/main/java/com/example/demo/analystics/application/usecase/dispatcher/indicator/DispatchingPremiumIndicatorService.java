package com.example.demo.analystics.application.usecase.dispatcher.indicator;

import com.example.demo.analystics.application.usecase.base.DispatchingAnalyticsService;
import com.example.demo.analystics.domain.dispatch_manager.AnalyticsMangerController;
import com.example.demo.analystics.domain.domain.indicator.open.PremiumIndicator;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DispatchingPremiumIndicatorService extends DispatchingAnalyticsService<PremiumKey, BigDecimal, PremiumIndicator> {

    public DispatchingPremiumIndicatorService(AnalyticsMangerController<PremiumKey, BigDecimal, PremiumIndicator, ?> controller) {
        super(controller);
    }
}
