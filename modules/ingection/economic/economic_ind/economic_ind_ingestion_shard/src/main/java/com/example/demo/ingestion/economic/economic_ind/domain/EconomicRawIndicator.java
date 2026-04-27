package com.example.demo.ingestion.economic.economic_ind.domain;

public record EconomicRawIndicator(
    EconomicIndicatorCode code,
    EconomicIndicatorValue value
) {
}
