package com.example.demo.analystics.application.usecase.revoke.indicator;

import com.example.demo.analystics.application.port.out.WriteAnalyticsStatePort;
import com.example.demo.analystics.application.usecase.base.RevokeAnalyticsStateService;
import com.example.demo.analystics.domain.dispatch_manager.AnalyticsMangerController;
import com.example.demo.analystics.domain.domain.candle.open.TickCandle;
import org.springframework.stereotype.Component;

@Component
public class RevokeTickIndicatorStateService extends RevokeAnalyticsStateService<TickCandle> {
    public RevokeTickIndicatorStateService(AnalyticsMangerController<?, ?, TickCandle, ?> core, WriteAnalyticsStatePort<TickCandle> writer) {
        super(core, writer);
    }
}
