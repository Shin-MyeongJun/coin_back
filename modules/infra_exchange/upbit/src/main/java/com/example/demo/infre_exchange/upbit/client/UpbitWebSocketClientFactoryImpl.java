package com.example.demo.infre_exchange.upbit.client;


import com.example.demo.infra_shard.connector.exchange.interfaces.ExchangeWebSocket;
import com.example.demo.infra_shard.connector.exchange.interfaces.ExchangeWsFactory;
import com.example.demo.infre_exchange.upbit.config.UpbitProperties;
import com.example.demo.infre_exchange.upbit.util.UpbitAuthTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okio.ByteString;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class UpbitWebSocketClientFactoryImpl implements ExchangeWsFactory<ByteString> {

    private final UpbitProperties props;
    private final UpbitAuthTokenProvider authTokenProvider;
    private final ObjectMapper objectMapper;
    
    @Override
    public ExchangeWebSocket<ByteString> create() {
        return new UpbitWebSocketClientImpl(props,authTokenProvider,objectMapper);
    }
}
