package com.example.demo.infre_exchange.client.impl;

import com.binance.connector.client.common.configuration.ClientConfiguration;
import com.binance.connector.client.common.configuration.SignatureConfiguration;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.DerivativesTradingUsdsFuturesRestApiUtil;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.api.DerivativesTradingUsdsFuturesRestApi;
import com.example.demo.infre_exchange.client.BinanceRawClient;
import com.example.demo.infre_exchange.config.BinanceProperties;
import org.springframework.stereotype.Component;

//기능은 외부api 의존

@Component
public class BinanceFutureRawClientImpl implements BinanceRawClient {
    private DerivativesTradingUsdsFuturesRestApi api;
    private final BinanceProperties binanceProperties;
    private final String EXCHANGE_NAME = "Binance";

    BinanceFutureRawClientImpl(BinanceProperties binanceProperties ){
        this.binanceProperties =binanceProperties;
        ClientConfiguration clientConfiguration = DerivativesTradingUsdsFuturesRestApiUtil.getClientConfiguration();
        SignatureConfiguration signatureConfiguration = new SignatureConfiguration();
        signatureConfiguration.setApiKey(binanceProperties.accessKey());
        signatureConfiguration.setSecretKey(binanceProperties.secretKey());
        clientConfiguration.setSignatureConfiguration(signatureConfiguration);
        this.api = new DerivativesTradingUsdsFuturesRestApi(clientConfiguration);
    }

    //걍 땡겨오기
    @Override
    public DerivativesTradingUsdsFuturesRestApi getClient() {
        if (api == null) {
            ClientConfiguration clientConfiguration = DerivativesTradingUsdsFuturesRestApiUtil.getClientConfiguration();
            SignatureConfiguration signatureConfiguration = new SignatureConfiguration();
            if(binanceProperties.accessKey() != null) {
                signatureConfiguration.setApiKey(binanceProperties.accessKey());
                signatureConfiguration.setPrivateKey(binanceProperties.secretKey());
            }
            clientConfiguration.setSignatureConfiguration(signatureConfiguration);
            api = new DerivativesTradingUsdsFuturesRestApi(clientConfiguration);
        }
        return api;
    }



}
