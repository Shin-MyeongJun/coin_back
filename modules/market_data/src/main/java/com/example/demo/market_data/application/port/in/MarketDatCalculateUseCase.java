package com.example.demo.market_data.application.port.in;

import com.example.demo.market_data.domain.domain.Fx;
import com.example.demo.market_data.domain.domain.Tick;

public interface MarketDatCalculateUseCase {
    void cal(String base);
    void put(Fx fx);
    void put(Tick tick);
}
