package com.example.demo.analystics.application.usecase.dispatcher.candle;

import com.example.demo.analystics.application.kernel.base.CandleManagerController;
import com.example.demo.analystics.application.usecase.base.dispatcher.DispatchingCandleService;
import com.example.demo.analystics.domain.domain.candle.value.PremiumDetailValue;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import org.springframework.stereotype.Component;

@Component
public class DispatchPremiumDetailCandleService extends DispatchingCandleService<PremiumKey, PremiumDetailValue> {

    public DispatchPremiumDetailCandleService(CandleManagerController<PremiumKey, PremiumDetailValue, ?, ?, ?, ?> controller) {
        super(controller);
    }
}
