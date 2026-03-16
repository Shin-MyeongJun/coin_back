package com.example.demo.analystics.application.usecase.dispatcher.indicator;

import com.example.demo.analystics.application.usecase.base.DispatchingAnalyticsService;
import com.example.demo.analystics.domain.dispatch_manager.AnalyticsMangerController;
import com.example.demo.analystics.domain.domain.indicator.open.TickIndicator;
import com.example.demo.analystics.domain.domain.key.TickKey;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DispatchingTickIndicatorService extends DispatchingAnalyticsService<TickKey, BigDecimal, TickIndicator> {
    public DispatchingTickIndicatorService(AnalyticsMangerController<TickKey, BigDecimal, TickIndicator, ?> controller) {
        super(controller);
    }
}
