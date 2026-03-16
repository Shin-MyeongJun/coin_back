package com.example.demo.analystics.application.usecase.interval_flush.candle;

import com.example.demo.analystics.application.port.out.WriteAnalyticsValuePort;
import com.example.demo.analystics.application.usecase.base.FlushAnalyticsService;
import com.example.demo.analystics.domain.dispatch_manager.AnalyticsMangerController;
import com.example.demo.analystics.domain.domain.candle.close.TickCloseCandle;
import org.springframework.stereotype.Component;

@Component
public class FlushTickCandleService extends FlushAnalyticsService<TickCloseCandle> {

    public FlushTickCandleService(AnalyticsMangerController<?, ?, ?, TickCloseCandle> controller, WriteAnalyticsValuePort<TickCloseCandle> writePort) {
        super(controller, writePort);
    }
}
