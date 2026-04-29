package com.example.demo.contracts.message.economic;

import java.math.BigDecimal;

public record EconomicIndicatorMessage(
        String indicatorCode,
        String type,
        String country,
        BigDecimal value,
        String observationDate,
        long releaseTimestamp,
        long timestamp,
        String source
) {}
