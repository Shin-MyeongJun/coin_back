package com.example.demo.ingestion.exchange.application.port.in;

public interface HandleMarketRawUseCase<RAW> {
    void process(RAW raw);
}
