package com.example.demo.ingestion.exchange.application;


import com.example.demo.infre_exchange.config.BinanceProperties;
import com.example.demo.ingestion.exchange.application.port.in.GetSymbolsUseCase;
import com.example.demo.ingestion.exchange.application.port.in.HandleMarketRawUseCase;
import com.example.demo.ingestion.exchange.application.port.out.SubscribeMarketDataPort;
import com.example.demo.ingestion.exchange.infra.BinanceMarketDataStream;
import org.springframework.stereotype.Component;

@Component
public class BinanceStreamManager extends ExchangeStreamManager {
    private final BinanceProperties properties;
    private final HandleMarketRawUseCase<String> useCase;

    public BinanceStreamManager(GetSymbolsUseCase symbolGetter, BinanceProperties properties, HandleMarketRawUseCase<String> useCase) {
        super(symbolGetter,1000L);
        this.properties = properties;
        this.useCase = useCase;
    }


    @Override
    protected SubscribeMarketDataPort createSubscriber() {
        return new BinanceMarketDataStream(
                properties,
                useCase
        );
    }
}
