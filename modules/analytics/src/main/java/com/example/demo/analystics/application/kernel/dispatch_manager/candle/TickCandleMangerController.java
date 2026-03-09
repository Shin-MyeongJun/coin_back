package com.example.demo.analystics.application.kernel.dispatch_manager.candle;

import com.example.demo.analystics.application.kernel.base.CandleManagerController;
import com.example.demo.analystics.domain.buffer.candle.TickCandleBuffer;
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
        TickCandleBuffer,
        TickCandleManager> {

    private final ClosingData<TickCandle, TickCloseCandle> closingData;


    @Override
    protected TickCandleManager createManager() {
        return new TickCandleManager(closingData);
    }
}
