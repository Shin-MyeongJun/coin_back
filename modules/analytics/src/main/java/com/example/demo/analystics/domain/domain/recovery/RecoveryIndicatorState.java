package com.example.demo.analystics.domain.domain.recovery;

import com.example.demo.analystics.domain.domain.indicator.open.state.IndicatorState;

public record RecoveryIndicatorState  (
        IndicatorState state,
        long timestamp
) implements RecoveryState {
}
