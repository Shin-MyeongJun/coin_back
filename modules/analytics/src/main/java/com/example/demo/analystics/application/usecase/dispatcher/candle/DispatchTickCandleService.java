package com.example.demo.analystics.application.usecase.dispatcher.candle;

import com.example.demo.analystics.application.usecase.base.DispatchingAnalyticsService;
import com.example.demo.analystics.domain.dispatch_manager.AnalyticsMangerController;
import com.example.demo.analystics.domain.domain.candle.open.TickCandle;
import com.example.demo.analystics.domain.domain.key.TickKey;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DispatchTickCandleService extends DispatchingAnalyticsService<TickKey, BigDecimal, TickCandle> {
    public DispatchTickCandleService(AnalyticsMangerController<TickKey, BigDecimal, TickCandle, ?> controller) {
        super(controller);
    }
}
