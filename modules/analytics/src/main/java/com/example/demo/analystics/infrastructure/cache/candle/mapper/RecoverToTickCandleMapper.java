package com.example.demo.analystics.infrastructure.cache.candle.mapper;

import com.example.demo.analystics.application.port.out.MappingRecoverToStatePort;
import com.example.demo.analystics.domain.domain.candle.open.TickCandle;
import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.analystics.domain.domain.recovery.RecoveryCandleState;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RecoverToTickCandleMapper implements MappingRecoverToStatePort<RecoveryCandleState<BigDecimal>, TickKey, TickCandle> {
    @Override
    public TickCandle toState(TickKey key  ,RecoveryCandleState<BigDecimal> recoveryCandleState) {
        return new TickCandle(
                key,
                recoveryCandleState.ohlcData(),
                recoveryCandleState.timestamp()
        );
    }
}
