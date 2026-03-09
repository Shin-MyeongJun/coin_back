package com.example.demo.ingestion.fx.application.port.in;

import reactor.core.publisher.Mono;

public interface GetFxRawUseCase {
    Mono<String> getRaw(String quote, String base);
}
