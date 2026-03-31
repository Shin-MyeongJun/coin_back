package com.example.demo.analystics.infrastructure.cache.indicator.mapper;

import com.example.demo.analystics.application.port.out.MappingRecoverToStatePort;
import com.example.demo.analystics.domain.domain.indicator.open.PremiumIndicator;
import com.example.demo.analystics.domain.domain.indicator.open.state.*;
import com.example.demo.analystics.domain.domain.key.IndicatorKey;
import com.example.demo.analystics.domain.domain.key.IndicatorPriceDataKey;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.domain.recovery.RecoveryIndicatorState;
import com.example.demo.analystics.domain.factory.indicator.value.IndicatorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecoverToPremiumIndicatorMapper implements MappingRecoverToStatePort<RecoveryIndicatorState, IndicatorPriceDataKey<PremiumKey>, PremiumIndicator> {

    private final IndicatorFactory<PremiumKey,PremiumIndicator> factory;

    @Override
    public PremiumIndicator toState(IndicatorPriceDataKey<PremiumKey> key, RecoveryIndicatorState recoveryIndicatorState) {
        PremiumKey dataKey = key.dataKey();
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
