package com.example.demo.ingestion.fx.application.port.in;

public interface IngestAndPublishFxUseCase {
    void run(String quote ,String base);
}
