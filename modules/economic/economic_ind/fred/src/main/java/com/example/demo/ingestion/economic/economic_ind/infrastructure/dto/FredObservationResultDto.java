package com.example.demo.ingestion.economic.economic_ind.infrastructure.dto;

public record FredObservationResultDto(
        String seriesId,
        String date,
        String value,
        String units,
        String frequency
) {}
