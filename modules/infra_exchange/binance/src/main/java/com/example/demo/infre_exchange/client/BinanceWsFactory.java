package com.example.demo.infre_exchange.client;


import com.binance.connector.client.derivatives_trading_usds_futures.websocket.api.api.DerivativesTradingUsdsFuturesWebSocketApi;

public interface BinanceWsFactory {
    DerivativesTradingUsdsFuturesWebSocketApi createPublic();

    DerivativesTradingUsdsFuturesWebSocketApi createPrivate();
}
