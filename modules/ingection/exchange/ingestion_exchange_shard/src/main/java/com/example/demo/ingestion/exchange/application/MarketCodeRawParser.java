package com.example.demo.ingestion.exchange.application;

import com.example.demo.contracts.message.raw.MarketCodeRawMessage;
import com.example.demo.ingestion.exchange.application.port.in.ParseMarketCodeRawUseCase;
import com.example.demo.ingestion.exchange.domain.MarketCodeKey;
import com.example.demo.ingestion.exchange.domain.MarketCodeValue;
import org.springframework.stereotype.Component;

@Component
public class MarketCodeRawParser implements ParseMarketCodeRawUseCase {
    @Override
    public MarketCodeKey getKey(MarketCodeRawMessage ms) {
        return new MarketCodeKey(
                ms.exchangeName(),

                ms.tradingPair()
        );
    }

    @Override
    public MarketCodeValue getValue(MarketCodeRawMessage ms) {
        return new MarketCodeValue(
                ms.exchangeType(),
                ms.asset(),
                ms.base()
        );
    }
}
