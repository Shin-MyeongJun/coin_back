package com.example.demo.economic_query.application.dto;

public record EconomicCalendarView(
        Long id,
        Long indCodeId,
        String releaseCode,
        Long releaseDate,
        String status,
        Long fetchedAt
) {
}
