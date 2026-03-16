package com.example.demo.analystics.application.usecase.revoke.candle;

import com.example.demo.analystics.application.port.out.WriteAnalyticsStatePort;
import com.example.demo.analystics.application.usecase.base.RevokeAnalyticsStateService;
import com.example.demo.analystics.domain.dispatch_manager.CandleManagerController;
import com.example.demo.analystics.domain.domain.candle.open.TickCandle;
import org.springframework.stereotype.Component;

@Component
public class RevokeTickCandleStateService extends RevokeAnalyticsStateService<TickCandle> {
    public RevokeTickCandleStateService(CandleManagerController<?, ?, TickCandle, ?, ?> core, WriteAnalyticsStatePort<TickCandle> writer) {
        super(core, writer);
    }
}
