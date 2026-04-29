package com.example.demo.ingestion.economic.crawling.domain.domain;

import java.math.BigDecimal;

public record GlobalIndex(
        String symbol,
        String name,
        BigDecimal price,
        long timestamp,
        String source
) {}
