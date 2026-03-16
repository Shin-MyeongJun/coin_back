package com.example.demo.analystics.domain.dispatch_manager.candle;

import com.example.demo.analystics.domain.dispatch_manager.CandleManagerController;
import com.example.demo.analystics.domain.domain.candle.close.TickCloseCandle;
import com.example.demo.analystics.domain.domain.candle.open.TickCandle;
import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.analystics.domain.manager.candle.TickCandleManager;
import com.example.demo.analystics.domain.service.ClosingData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class TickCandleMangerController extends CandleManagerController<
        TickKey,
        BigDecimal,
        TickCandle,
        TickCloseCandle,
        TickCandleManager> {

    private final ClosingData<TickCandle, TickCloseCandle> closingData;


    @Override
    protected TickCandleManager createManager() {
        return new TickCandleManager(closingData);
    }
}
