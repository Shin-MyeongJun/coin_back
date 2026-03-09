package com.example.demo.ingestion.exchange.application;


import com.example.demo.infre_exchange.upbit.client.UpbitWebSocketClientImpl;
import com.example.demo.infre_exchange.upbit.config.UpbitProperties;
import com.example.demo.infre_exchange.upbit.util.UpbitAuthTokenProvider;
import com.example.demo.ingestion.exchange.application.port.in.GetSymbolsUseCase;
import com.example.demo.ingestion.exchange.application.port.in.HandleMarketRawUseCase;
import com.example.demo.ingestion.exchange.application.port.out.SubscribeMarketDataPort;
import com.example.demo.ingestion.exchange.infra.UpbitMarketDataStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import okio.ByteString;
import org.springframework.stereotype.Component;

@Component
public class UpbitStreamManager extends ExchangeStreamManager {
    private final HandleMarketRawUseCase<ByteString> usecase;
    private  final UpbitProperties properties;
    private final UpbitAuthTokenProvider provider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UpbitStreamManager(GetSymbolsUseCase getter, HandleMarketRawUseCase<ByteString> usecase, UpbitProperties properties, UpbitAuthTokenProvider provider) {
        super(getter,1000L);
        this.usecase = usecase;
        this.properties = properties;
        this.provider = provider;
    }

    @Override
    protected SubscribeMarketDataPort createSubscriber() {
        return new UpbitMarketDataStream(
                new UpbitWebSocketClientImpl(properties,provider,objectMapper),
                usecase
        );
    }
}
