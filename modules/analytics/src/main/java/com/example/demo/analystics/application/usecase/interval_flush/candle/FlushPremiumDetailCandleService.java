package com.example.demo.analystics.application.usecase.interval_flush.candle;

import com.example.demo.analystics.application.kernel.base.CandleManagerController;
import com.example.demo.analystics.application.usecase.base.interval_flush.FlushCandleService;
import com.example.demo.analystics.domain.domain.candle.close.PremiumDetailCloseCandle;
import org.springframework.stereotype.Component;

@Component
public class FlushPremiumDetailCandleService extends FlushCandleService<PremiumDetailCloseCandle> {

    public FlushPremiumDetailCandleService(CandleManagerController<?, ?, ?, PremiumDetailCloseCandle, ?, ?> controller) {
        super(controller);
    }
}
