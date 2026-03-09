package com.example.demo.analystics.application.usecase.dispatcher.candle;

import com.example.demo.analystics.application.kernel.base.CandleManagerController;
import com.example.demo.analystics.application.usecase.base.dispatcher.DispatchingCandleService;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DispatchPremiumCandleService extends DispatchingCandleService<PremiumKey, BigDecimal> {

    public DispatchPremiumCandleService(CandleManagerController<PremiumKey, BigDecimal, ?, ?, ?, ?> controller) {
        super(controller);
    }
}
