package com.example.demo.analystics.infrastructure.cache.indicator.codec.base;

import com.example.demo.analystics.domain.domain.indicator.TradeIndicatorType;
import com.example.demo.analystics.domain.domain.indicator.open.state.IndicatorState;
import com.example.demo.analystics.domain.domain.recovery.RecoveryIndicatorState;

import java.util.Set;

public interface IndicatorStateCodec<STATE extends IndicatorState> {
    public byte[] encode(STATE state, long timestamp);
    public RecoveryIndicatorState decode(byte[] bytes);
    public Set<TradeIndicatorType> supportedTradeIndicatorTypes();
}
