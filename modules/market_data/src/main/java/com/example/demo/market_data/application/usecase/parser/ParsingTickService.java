package com.example.demo.market_data.application.usecase.parser;

import com.example.demo.market_data.application.port.in.ParsingValUseCase;
import com.example.demo.market_data.domain.domain.Tick;
import org.springframework.stereotype.Component;

@Component
public class ParsingTickService implements ParsingValUseCase<Tick, Long> {
    @Override
    public Long getKey(Tick tick) {
        return  tick.marketCodeId();
    }
}
