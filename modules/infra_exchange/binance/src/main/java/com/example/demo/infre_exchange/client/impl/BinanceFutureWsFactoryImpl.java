package com.example.demo.infre_exchange.client.impl;


import com.example.demo.infra_shard.connector.exchange.interfaces.ExchangeWebSocket;
import com.example.demo.infra_shard.connector.exchange.interfaces.ExchangeWsFactory;
import com.example.demo.infre_exchange.config.BinanceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BinanceFutureWsFactoryImpl implements ExchangeWsFactory {

    private  final BinanceProperties properties;

    @Override
    public ExchangeWebSocket create() {
        return new BinanceFutureWebSocketImpl(properties);
    }





}
