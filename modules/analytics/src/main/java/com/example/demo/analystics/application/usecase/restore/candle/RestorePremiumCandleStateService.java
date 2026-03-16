package com.example.demo.analystics.application.usecase.restore.candle;

import com.example.demo.analystics.application.port.out.MappingRecoverToStatePort;
import com.example.demo.analystics.application.port.out.ReadAnalyticsStatePort;
import com.example.demo.analystics.application.usecase.base.RestoreAnalyticsStateService;
import com.example.demo.analystics.domain.dispatch_manager.CandleManagerController;
import com.example.demo.analystics.domain.domain.candle.open.PremiumCandle;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.domain.recovery.RecoveryCandleState;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RestorePremiumCandleStateService
        extends RestoreAnalyticsStateService<PremiumKey,
                RecoveryCandleState<BigDecimal>,
                PremiumCandle
                >
{


    public RestorePremiumCandleStateService(ReadAnalyticsStatePort<PremiumKey, RecoveryCandleState<BigDecimal>> reader, MappingRecoverToStatePort<RecoveryCandleState<BigDecimal>, PremiumKey, PremiumCandle> mapper, CandleManagerController<?, ?, PremiumCandle, ?, ?> core) {
        super(reader, mapper, core);
    }
}
