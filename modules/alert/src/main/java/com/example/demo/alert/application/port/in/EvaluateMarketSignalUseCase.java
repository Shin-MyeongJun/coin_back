package com.example.demo.alert.application.port.in;

import com.example.demo.alert.domain.signal.MarketSignal;

public interface EvaluateMarketSignalUseCase {
    void onSignal(MarketSignal signal);
}
