package com.example.demo.analystics.infrastructure.cache.candle.mapper;


import com.example.demo.analystics.application.port.out.MappingRecoverToStatePort;
import com.example.demo.analystics.domain.domain.candle.open.PremiumCandle;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.domain.recovery.RecoveryCandleState;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RecoverToPremiumCandleMapper implements MappingRecoverToStatePort<RecoveryCandleState<BigDecimal>, PremiumKey, PremiumCandle> {
    @Override
    public PremiumCandle toState(PremiumKey key , RecoveryCandleState<BigDecimal> recoveryCandleState) {
        return new PremiumCandle(
                key,
                recoveryCandleState.ohlcData(),
                recoveryCandleState.timestamp()
        );
    }
}
