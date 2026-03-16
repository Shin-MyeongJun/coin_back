package com.example.demo.analystics.application.usecase.interval_flush.indicator;

import com.example.demo.analystics.application.port.out.WriteAnalyticsValuePort;
import com.example.demo.analystics.application.usecase.base.FlushAnalyticsService;
import com.example.demo.analystics.domain.dispatch_manager.AnalyticsMangerController;
import com.example.demo.analystics.domain.domain.indicator.close.PremiumCloseIndicator;
import org.springframework.stereotype.Component;

@Component
public class FlushPremiumIndicatorService extends FlushAnalyticsService<PremiumCloseIndicator> {

    public FlushPremiumIndicatorService(AnalyticsMangerController<?, ?, ?, PremiumCloseIndicator> controller, WriteAnalyticsValuePort<PremiumCloseIndicator> writePort) {
        super(controller, writePort);
    }
}