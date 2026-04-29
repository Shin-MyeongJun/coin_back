package com.example.demo.ingestion.economic.crawling.infrastructure.crawler.yahoo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YahooQuoteResponse(
        QuoteResponseBody quoteResponse
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record QuoteResponseBody(
            List<YahooQuote> result,
            Object error
    ) {}
}
