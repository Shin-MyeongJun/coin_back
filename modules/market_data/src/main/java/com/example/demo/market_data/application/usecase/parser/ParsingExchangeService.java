package com.example.demo.market_data.application.usecase.parser;

import com.example.demo.market_data.application.port.in.ParsingValUseCase;
import com.example.demo.market_data.domain.domain.snapshot.ExchangeSnapShot;
import com.example.demo.market_data.domain.domain.snapshot.ExchangeSnapShotVal;
import org.springframework.stereotype.Component;

@Component
public class ParsingExchangeService implements ParsingValUseCase<ExchangeSnapShot, ExchangeSnapShotVal> {
    @Override
    public ExchangeSnapShotVal getKey(ExchangeSnapShot domain) {
        return new ExchangeSnapShotVal(
                domain.name(),
                domain.exchangeType(),
                domain.quote()
        );
    }
}
