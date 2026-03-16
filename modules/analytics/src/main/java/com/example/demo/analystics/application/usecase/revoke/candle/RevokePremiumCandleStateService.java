package com.example.demo.analystics.application.usecase.revoke.candle;

import com.example.demo.analystics.application.port.out.WriteAnalyticsStatePort;
import com.example.demo.analystics.application.usecase.base.RevokeAnalyticsStateService;
import com.example.demo.analystics.domain.dispatch_manager.AnalyticsMangerController;
import com.example.demo.analystics.domain.domain.candle.open.PremiumCandle;
import org.springframework.stereotype.Component;

@Component
public class RevokePremiumCandleStateService extends RevokeAnalyticsStateService<PremiumCandle> {
    public RevokePremiumCandleStateService(AnalyticsMangerController<?, ?, PremiumCandle, ?> core, WriteAnalyticsStatePort<PremiumCandle> writer) {
        super(core, writer);
    }
}
