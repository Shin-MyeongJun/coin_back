package com.example.demo.ingestion.economic.crawling.infrastructure.crawler.yahoo;

import com.example.demo.ingestion.economic.crawling.infrastructure.config.CrawlingProperties;
import com.example.demo.ingestion.economic.crawling.infrastructure.crawler.yahoo.dto.YahooQuote;
import com.example.demo.ingestion.economic.crawling.infrastructure.crawler.yahoo.dto.YahooQuoteResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class YahooFinanceClient {

    private final WebClient webClient;
    private final CrawlingProperties properties;

    public YahooFinanceClient(CrawlingProperties properties) {
        this.properties = properties;
        this.webClient = WebClient.builder()
                .baseUrl(properties.getYahoo().getBaseUrl())
                .defaultHeader(HttpHeaders.USER_AGENT, properties.getYahoo().getUserAgent())
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();
    }

    public List<YahooQuote> fetchQuotes() {
        String symbols = String.join(",", properties.getYahoo().getSymbols());
        YahooQuoteResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v7/finance/quote")
                        .queryParam("symbols", symbols)
                        .queryParam("fields", "symbol,longName,shortName,regularMarketPrice,regularMarketTime")
                        .build())
                .retrieve()
                .bodyToMono(YahooQuoteResponse.class)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(500)).maxBackoff(Duration.ofSeconds(3)))
                .onErrorResume(e -> {
                    log.warn("Yahoo Finance fetch failed: {}", e.getMessage());
                    return reactor.core.publisher.Mono.empty();
                })
                .block(Duration.ofSeconds(10));

        if (response == null || response.quoteResponse() == null
                || response.quoteResponse().result() == null) {
            return List.of();
        }
        return response.quoteResponse().result();
    }
}
