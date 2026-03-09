package com.example.demo.ingestion.exchange.infra;


import com.example.demo.infre_exchange.upbit.client.UpbitWebSocketClientImpl;
import com.example.demo.ingestion.exchange.application.port.in.HandleMarketRawUseCase;
import com.example.demo.ingestion.exchange.application.port.out.SubscribeMarketDataPort;
import lombok.RequiredArgsConstructor;
import okio.ByteString;

import java.util.List;


@RequiredArgsConstructor
public class UpbitMarketDataStream implements SubscribeMarketDataPort {

    private final UpbitWebSocketClientImpl stream;
    private final HandleMarketRawUseCase<ByteString> useCase;



    @Override
    public void subscribe(List<String> codes) {
        stream.getOrderbook(codes,useCase::process);
    }

    @Override
    public void unsubscribe() {
        stream.disconnect();
    }
}
