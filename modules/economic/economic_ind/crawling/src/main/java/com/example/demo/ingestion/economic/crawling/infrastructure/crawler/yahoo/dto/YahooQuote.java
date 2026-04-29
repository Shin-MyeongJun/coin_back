package com.example.demo.ingestion.economic.crawling.infrastructure.crawler.yahoo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YahooQuote(
        String symbol,
        String longName,
        String shortName,
        @JsonProperty("regularMarketPrice") double regularMarketPrice,
        @JsonProperty("regularMarketTime") long regularMarketTime
) {}
