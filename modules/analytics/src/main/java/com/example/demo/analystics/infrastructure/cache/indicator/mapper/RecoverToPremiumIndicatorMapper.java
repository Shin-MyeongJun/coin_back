package com.example.demo.analystics.infrastructure.cache.indicator.mapper;

import com.example.demo.analystics.application.port.out.MappingRecoverToStatePort;
import com.example.demo.analystics.domain.domain.candle.open.PremiumCandle;
import com.example.demo.analystics.domain.domain.indicator.open.PremiumIndicator;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.domain.recovery.RecoveryIndicatorState;

public class RecoverToPremiumIndicatorMapper implements MappingRecoverToStatePort<RecoveryIndicatorState, PremiumKey, PremiumIndicator> {

    @Override
    public PremiumIndicator toState(PremiumKey key, RecoveryIndicatorState recoveryIndicatorState) {
        recoveryIndicatorState.state();
    }
}
