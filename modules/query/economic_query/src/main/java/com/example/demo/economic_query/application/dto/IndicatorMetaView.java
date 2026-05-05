package com.example.demo.economic_query.application.dto;

public record IndicatorMetaView(
        Long id,
        String indicatorCode,
        String country,
        String type,
        String frequency,
        String unit
) {
}
