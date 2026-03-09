package com.example.demo.ingestion.exchange.infra;


import com.example.demo.infre_exchange.client.impl.BinanceFutureWebSocketImpl;
import com.example.demo.infre_exchange.config.BinanceProperties;
import com.example.demo.ingestion.exchange.application.port.in.HandleMarketRawUseCase;
import com.example.demo.ingestion.exchange.application.port.out.SubscribeMarketDataPort;

import java.util.List;


public class BinanceMarketDataStream implements SubscribeMarketDataPort {

    private final BinanceFutureWebSocketImpl stream ;
    private final HandleMarketRawUseCase<String> useCase;

    public BinanceMarketDataStream(BinanceProperties properties, HandleMarketRawUseCase<String> useCase){
        this.stream = new BinanceFutureWebSocketImpl(properties);
        this.useCase = useCase;
    }


    @Override
    public void subscribe(List<String> codes) {
        stream.getTicker(codes, useCase::process);

    }

    @Override
    public void unsubscribe() {
        stream.disconnect();
    }
}
