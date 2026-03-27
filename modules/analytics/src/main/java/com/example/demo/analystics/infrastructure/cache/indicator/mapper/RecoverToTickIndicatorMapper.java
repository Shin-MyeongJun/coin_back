package com.example.demo.analystics.infrastructure.cache.indicator.mapper;

import com.example.demo.analystics.application.port.out.MappingRecoverToStatePort;
import com.example.demo.analystics.domain.domain.indicator.open.TickIndicator;
import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.analystics.domain.domain.recovery.RecoveryIndicatorState;

public class RecoverToTickIndicatorMapper implements MappingRecoverToStatePort<RecoveryIndicatorState, TickKey, TickIndicator> {
    @Override
    public TickIndicator toState(TickKey key, RecoveryIndicatorState recoveryIndicatorState) {
        return null;
    }
}
