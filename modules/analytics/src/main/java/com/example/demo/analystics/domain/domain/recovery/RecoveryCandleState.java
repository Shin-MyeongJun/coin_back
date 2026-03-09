package com.example.demo.analystics.domain.domain.recovery;

import com.example.demo.analystics.domain.domain.candle.value.OhlcData;

public record RecoveryCandleState<VAL extends Comparable<VAL>>(
        OhlcData<VAL> ohlcData,
        long timestamp
) implements RecoveryState {
}
