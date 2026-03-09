package com.example.demo.infre_exchange.client;

import com.binance.connector.client.derivatives_trading_usds_futures.rest.api.DerivativesTradingUsdsFuturesRestApi;

public interface BinanceRawClient {
    DerivativesTradingUsdsFuturesRestApi getClient();

}
