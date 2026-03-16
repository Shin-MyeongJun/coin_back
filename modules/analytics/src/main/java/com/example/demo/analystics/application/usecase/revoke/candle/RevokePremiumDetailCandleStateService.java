package com.example.demo.analystics.application.usecase.revoke.candle;

import com.example.demo.analystics.application.port.out.WriteAnalyticsStatePort;
import com.example.demo.analystics.application.usecase.base.RevokeAnalyticsStateService;
import com.example.demo.analystics.domain.dispatch_manager.CandleManagerController;
import com.example.demo.analystics.domain.domain.candle.open.PremiumDetailCandle;
import org.springframework.stereotype.Component;

@Component
public class RevokePremiumDetailCandleStateService extends RevokeAnalyticsStateService<PremiumDetailCandle> {
    public RevokePremiumDetailCandleStateService(CandleManagerController<?, ?, PremiumDetailCandle, ?, ?> core, WriteAnalyticsStatePort<PremiumDetailCandle> writer) {
        super(core, writer);
    }
}
