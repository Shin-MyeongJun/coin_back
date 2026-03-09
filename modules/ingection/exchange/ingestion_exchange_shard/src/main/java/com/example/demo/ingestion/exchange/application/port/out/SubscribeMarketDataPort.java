package com.example.demo.ingestion.exchange.application.port.out;

import java.util.List;

public interface SubscribeMarketDataPort {

    void subscribe(List<String> codes);
    void unsubscribe();
}
