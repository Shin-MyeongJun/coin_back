package com.example.demo.meta_data_query.application.dto;

public record MappingIntegrityResult(
        Long exchangeId,
        String exchangeName,
        Long marketCodeId,
        String tradingPair,
        String issueType
) {
}
