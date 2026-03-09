package com.example.demo.ingestion.exchange.application.port.out;

public interface RawPublishPort<MESSAGE> {
    void publish(MESSAGE message);
}
