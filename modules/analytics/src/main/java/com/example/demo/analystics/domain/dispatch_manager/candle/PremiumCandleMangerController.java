package com.example.demo.analystics.domain.dispatch_manager.candle;

import com.example.demo.analystics.domain.dispatch_manager.CandleManagerController;
import com.example.demo.analystics.domain.domain.candle.close.PremiumCloseCandle;
import com.example.demo.analystics.domain.domain.candle.open.PremiumCandle;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.manager.candle.PremiumCandleManager;
import com.example.demo.analystics.domain.service.ClosingData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class PremiumCandleMangerController extends CandleManagerController<
        PremiumKey,
        BigDecimal,
        PremiumCandle,
        PremiumCloseCandle,
        PremiumCandleManager> {

    private final ClosingData<PremiumCandle, PremiumCloseCandle> closingData;

    @Override
    protected PremiumCandleManager createManager() {
        return new PremiumCandleManager(closingData);
    }
}
