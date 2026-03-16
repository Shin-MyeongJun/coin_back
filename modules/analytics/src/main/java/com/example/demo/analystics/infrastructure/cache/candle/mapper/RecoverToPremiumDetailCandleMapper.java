package com.example.demo.analystics.infrastructure.cache.candle.mapper;

import com.example.demo.analystics.application.port.out.MappingRecoverToStatePort;
import com.example.demo.analystics.domain.domain.candle.open.PremiumDetailCandle;
import com.example.demo.analystics.domain.domain.candle.value.PremiumDetailValue;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.domain.recovery.RecoveryCandleState;

public class RecoverToPremiumDetailCandleMapper
        implements MappingRecoverToStatePort<RecoveryCandleState<PremiumDetailValue>, PremiumKey, PremiumDetailCandle> {
@Override
public PremiumDetailCandle toState(PremiumKey key , RecoveryCandleState<PremiumDetailValue> recoveryCandleState) {
    return new PremiumDetailCandle(
            key,
            recoveryCandleState.ohlcData(),
            recoveryCandleState.timestamp()
    );
}
}
