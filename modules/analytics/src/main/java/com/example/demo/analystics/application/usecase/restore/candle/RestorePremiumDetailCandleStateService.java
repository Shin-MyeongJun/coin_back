package com.example.demo.analystics.application.usecase.restore.candle;

import com.example.demo.analystics.application.port.out.MappingRecoverToStatePort;
import com.example.demo.analystics.application.port.out.ReadAnalyticsStatePort;
import com.example.demo.analystics.application.usecase.base.RestoreAnalyticsStateService;
import com.example.demo.analystics.domain.dispatch_manager.CandleManagerController;
import com.example.demo.analystics.domain.domain.candle.open.PremiumDetailCandle;
import com.example.demo.analystics.domain.domain.candle.value.PremiumDetailValue;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.domain.recovery.RecoveryCandleState;

public class RestorePremiumDetailCandleStateService extends RestoreAnalyticsStateService<PremiumKey,
        RecoveryCandleState<PremiumDetailValue>,
        PremiumDetailCandle
        > {

    public RestorePremiumDetailCandleStateService(ReadAnalyticsStatePort<PremiumKey, RecoveryCandleState<PremiumDetailValue>> reader, MappingRecoverToStatePort<RecoveryCandleState<PremiumDetailValue>, PremiumKey, PremiumDetailCandle> mapper, CandleManagerController<?, ?, PremiumDetailCandle, ?, ?> core) {
        super(reader, mapper, core);
    }
}
