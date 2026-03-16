package com.example.demo.analystics.application.usecase.interval_flush.candle;

import com.example.demo.analystics.application.port.out.WriteAnalyticsValuePort;
import com.example.demo.analystics.application.usecase.base.FlushAnalyticsService;
import com.example.demo.analystics.domain.dispatch_manager.AnalyticsMangerController;
import com.example.demo.analystics.domain.domain.candle.close.PremiumDetailCloseCandle;
import org.springframework.stereotype.Component;

@Component
public class FlushPremiumDetailCandleService extends FlushAnalyticsService<PremiumDetailCloseCandle> {


    public FlushPremiumDetailCandleService(AnalyticsMangerController<?, ?, ?, PremiumDetailCloseCandle> controller, WriteAnalyticsValuePort<PremiumDetailCloseCandle> writePort) {
        super(controller, writePort);
    }
}
