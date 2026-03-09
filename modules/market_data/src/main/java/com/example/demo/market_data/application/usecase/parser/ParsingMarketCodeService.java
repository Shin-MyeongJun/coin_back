package com.example.demo.market_data.application.usecase.parser;

import com.example.demo.market_data.application.port.in.ParsingValUseCase;
import com.example.demo.market_data.domain.domain.snapshot.MarketCodeSnapShot;
import com.example.demo.market_data.domain.domain.snapshot.MarketCodeSnapShotVal;
import org.springframework.stereotype.Component;

@Component
public class ParsingMarketCodeService implements ParsingValUseCase<MarketCodeSnapShot, MarketCodeSnapShotVal> {
    @Override
    public MarketCodeSnapShotVal getKey(MarketCodeSnapShot domain) {
        return new MarketCodeSnapShotVal(
                domain.exchangeId(),
                domain.baseAsset(),
                domain.tradingPair()
        );
    }
}
