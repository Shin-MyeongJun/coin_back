package com.example.demo.infre_exchange.client.impl;


import com.example.demo.infre_exchange.config.BinanceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class BinanceFutureConnector {

    private final BinanceProperties properties;
    private final WebClient client = WebClient.builder()
            .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(configurer -> configurer
                            .defaultCodecs()
                            .maxInMemorySize(2 * 1024 * 1024)) // 2MB
                    .build())
            .build();

    public Mono<String> getMarketCodes(){
        return client.get().uri(properties.future().usdt().marketData().exchangeInfo())
                .retrieve()
                .bodyToMono(String.class);

    }

    public Mono<String> getOrderBook(String symbol){
        return client.get()
                .uri(uri->uri
                        .host(properties.future().usdt().baseUrl())
                        .path(properties.future().usdt().marketData().orderBook())
                        .queryParam("symbol",symbol)
                        .queryParam("limit", 10)
                        .build()
                )
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> getTicker(String symbol){
        return client.get()
                .uri(uri->uri.path(properties.future().usdt().marketData().bookTicker())
                        .queryParam("symbol",symbol)
                        .build()
                )
                .retrieve()
                .bodyToMono(String.class);
    }





}
