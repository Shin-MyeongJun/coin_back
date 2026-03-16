package com.example.demo.analystics.application.usecase.interval_flush.indicator;

import com.example.demo.analystics.application.port.out.WriteAnalyticsValuePort;
import com.example.demo.analystics.application.usecase.base.FlushAnalyticsService;
import com.example.demo.analystics.domain.dispatch_manager.AnalyticsMangerController;
import com.example.demo.analystics.domain.domain.indicator.close.TickCloseIndicator;
import org.springframework.stereotype.Component;

@Component
public class FlushTickIndicatorService extends FlushAnalyticsService<TickCloseIndicator> {
    public FlushTickIndicatorService(AnalyticsMangerController<?, ?, ?, TickCloseIndicator> controller, WriteAnalyticsValuePort<TickCloseIndicator> writePort) {
        super(controller, writePort);
    }
}
