package com.example.demo.analystics.application.kernel.dispatch_manager.candle;

import com.example.demo.analystics.application.kernel.base.CandleManagerController;
import com.example.demo.analystics.domain.buffer.candle.PremiumDetailCandleBuffer;
import com.example.demo.analystics.domain.domain.candle.close.PremiumDetailCloseCandle;
import com.example.demo.analystics.domain.domain.candle.open.PremiumDetailCandle;
import com.example.demo.analystics.domain.domain.candle.value.PremiumDetailValue;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.manager.candle.PremiumDetailCandleManager;
import com.example.demo.analystics.domain.service.ClosingData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PremiumDetailCandleMangerController  extends CandleManagerController<
        PremiumKey,
        PremiumDetailValue,
        PremiumDetailCandle,
        PremiumDetailCloseCandle,
        PremiumDetailCandleBuffer,
        PremiumDetailCandleManager> {

    private final ClosingData<PremiumDetailCandle, PremiumDetailCloseCandle> closingData;

    @Override
    protected PremiumDetailCandleManager createManager() {
        return new PremiumDetailCandleManager(closingData);
    }
}

