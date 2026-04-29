package com.example.demo.ingestion.economic.economic_ind.domain;

import com.example.demo.ingestion.economic.economic_ind.domain.enums.IndicatorUnit;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.ReleaseFrequency;

public record EconomicIndicatorCode(
        String indicatorCode,
        String country,
        String type, //데이터 소스
        ReleaseFrequency frequency,     // "Monthly"
        IndicatorUnit unit //단위
) {
    public static EconomicIndicatorCode of(String country, String type, ReleaseFrequency frequency, IndicatorUnit unit) {
        String generatedCode = String.format("%s_%s_%s", country, type, frequency);
        return new EconomicIndicatorCode(generatedCode, country, type, frequency, unit);
    }
}
