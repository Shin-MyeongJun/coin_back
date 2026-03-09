package com.example.demo.analystics.application.usecase.dispatcher.candle;

import com.example.demo.analystics.application.kernel.base.CandleManagerController;
import com.example.demo.analystics.application.usecase.base.dispatcher.DispatchingCandleService;
import com.example.demo.analystics.domain.domain.key.TickKey;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DispatchTickCandleService extends DispatchingCandleService<TickKey, BigDecimal> {
    public DispatchTickCandleService(CandleManagerController<TickKey, BigDecimal, ?, ?, ?, ?> controller) {
        super(controller);
    }
}
