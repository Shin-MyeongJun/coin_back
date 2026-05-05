package com.example.demo.meta_data_query.application.dto;

public record ExchangeView(
        Long id,
        String name,
        String exchangeType,
        String quote,
        String country,
        String status
) {
}
