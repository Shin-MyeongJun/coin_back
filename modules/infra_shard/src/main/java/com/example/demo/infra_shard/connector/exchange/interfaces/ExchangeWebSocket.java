package com.example.demo.infra_shard.connector.exchange.interfaces;

import java.util.List;
import java.util.function.Consumer;

public interface ExchangeWebSocket<T> {
    void getTicker(List<String> marketCodes, Consumer<T> onMessage);
    void getOrderbook(List<String> marketCodes, Consumer<T> onMessage);
    void getMyOrder(Consumer<T> onMessage);
    void getMyAsset(Consumer<T> onMessage);
    void disconnect();
}
