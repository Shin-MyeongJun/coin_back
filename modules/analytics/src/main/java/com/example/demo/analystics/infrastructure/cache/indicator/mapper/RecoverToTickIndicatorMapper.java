package com.example.demo.analystics.infrastructure.cache.indicator.mapper;

import com.example.demo.analystics.application.port.out.MappingRecoverToStatePort;
import com.example.demo.analystics.domain.domain.indicator.open.TickIndicator;
import com.example.demo.analystics.domain.domain.indicator.open.state.IndicatorState;
import com.example.demo.analystics.domain.domain.key.IndicatorKey;
import com.example.demo.analystics.domain.domain.key.IndicatorPriceDataKey;
import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.analystics.domain.domain.recovery.RecoveryIndicatorState;
import com.example.demo.analystics.domain.factory.indicator.value.IndicatorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecoverToTickIndicatorMapper implements MappingRecoverToStatePort<RecoveryIndicatorState, IndicatorPriceDataKey<TickKey>, TickIndicator> {

    private final IndicatorFactory<TickKey, TickIndicator> factory;

    @Override
    public TickIndicator toState(IndicatorPriceDataKey<TickKey> key, RecoveryIndicatorState recoveryIndicatorState) {
        TickKey dataKey = key.dataKey();
        IndicatorKey indKey = key.indicatorKey();
        IndicatorState state = recoveryIndicatorState.state();
        long timestamp = recoveryIndicatorState.timestamp();
        return factory.createIndicatorFromState(
                dataKey,
                indKey,
                state,
                timestamp
        );
    }
}
