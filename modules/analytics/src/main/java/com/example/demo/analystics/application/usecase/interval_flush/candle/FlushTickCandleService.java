package com.example.demo.analystics.application.usecase.interval_flush.candle;

import com.example.demo.analystics.application.kernel.base.CandleManagerController;
import com.example.demo.analystics.application.usecase.base.interval_flush.FlushCandleService;
import com.example.demo.analystics.domain.domain.candle.close.TickCloseCandle;
import org.springframework.stereotype.Component;

@Component
public class FlushTickCandleService extends FlushCandleService<TickCloseCandle> {
    public FlushTickCandleService(CandleManagerController<?, ?, ?, TickCloseCandle, ?, ?> controller) {
        super(controller);
    }
}
