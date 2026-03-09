package com.example.demo.infra_shard.connector.exchange.interfaces;




public interface ExchangeWsFactory<T> {
    ExchangeWebSocket<T> create();
}
