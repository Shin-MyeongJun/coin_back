package com.example.demo.analystics.application.usecase.restore.candle;

import com.example.demo.analystics.application.port.out.MappingRecoverToStatePort;
import com.example.demo.analystics.application.port.out.ReadAnalyticsStatePort;
import com.example.demo.analystics.application.usecase.base.RestoreAnalyticsStateService;
import com.example.demo.analystics.domain.dispatch_manager.CandleManagerController;
import com.example.demo.analystics.domain.domain.candle.open.TickCandle;
import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.analystics.domain.domain.recovery.RecoveryCandleState;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RestoreTickCandleStateService extends RestoreAnalyticsStateService<TickKey,
        RecoveryCandleState<BigDecimal>,
        TickCandle
        > {

    public RestoreTickCandleStateService(ReadAnalyticsStatePort<TickKey, RecoveryCandleState<BigDecimal>> reader, MappingRecoverToStatePort<RecoveryCandleState<BigDecimal>, TickKey, TickCandle> mapper, CandleManagerController<?, ?, TickCandle, ?, ?> core) {
        super(reader, mapper, core);
    }
}
