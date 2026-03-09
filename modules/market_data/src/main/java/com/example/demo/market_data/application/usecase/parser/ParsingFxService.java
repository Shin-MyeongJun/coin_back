package com.example.demo.market_data.application.usecase.parser;

import com.example.demo.market_data.application.port.in.ParsingValUseCase;
import com.example.demo.market_data.domain.domain.Fx;
import com.example.demo.market_data.domain.domain.FxKey;
import org.springframework.stereotype.Component;

@Component
public class ParsingFxService implements ParsingValUseCase<Fx, FxKey> {
    @Override
    public FxKey getKey(Fx fx) {
        return new FxKey(fx.base(),fx.compare());
    }
}
