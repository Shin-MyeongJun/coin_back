package com.example.demo.analystics.domain.dispatch_manager.candle;

import com.example.demo.analystics.domain.dispatch_manager.CandleManagerController;
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
        PremiumDetailCandleManager> {

    private final ClosingData<PremiumDetailCandle, PremiumDetailCloseCandle> closingData;

    @Override
    protected PremiumDetailCandleManager createManager() {
        return new PremiumDetailCandleManager(closingData);
    }
}

