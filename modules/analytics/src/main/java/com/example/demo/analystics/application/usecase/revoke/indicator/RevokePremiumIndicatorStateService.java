package com.example.demo.analystics.application.usecase.revoke.indicator;

import com.example.demo.analystics.application.port.out.WriteAnalyticsStatePort;
import com.example.demo.analystics.application.usecase.base.RevokeAnalyticsStateService;
import com.example.demo.analystics.domain.dispatch_manager.AnalyticsMangerController;
import com.example.demo.analystics.domain.domain.candle.open.PremiumCandle;
import org.springframework.stereotype.Component;

@Component
public class RevokePremiumIndicatorStateService extends RevokeAnalyticsStateService<PremiumCandle> {
    public RevokePremiumIndicatorStateService(AnalyticsMangerController<?, ?, PremiumCandle, ?> core, WriteAnalyticsStatePort<PremiumCandle> writer) {
        super(core, writer);
    }
}
