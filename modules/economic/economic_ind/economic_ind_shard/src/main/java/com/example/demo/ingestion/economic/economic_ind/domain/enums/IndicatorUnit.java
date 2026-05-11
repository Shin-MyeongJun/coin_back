package com.example.demo.ingestion.economic.economic_ind.domain.enums;

public enum IndicatorUnit {
    PERCENT("PERCENT"),
    INDEX("INDEX"),
    CURRENCY("CURRENCY"),
    COUNT("COUNT"),
    VOLUME("VOLUME"),
    POINTS("POINTS"),
    UNKNOWN("UNKNOWN");

    private String value;

    IndicatorUnit(String value) {
        this.value = value;
    }

    public static IndicatorUnit fromValue(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }
        String normalized = value.trim();
        for (IndicatorUnit indicatorUnit : IndicatorUnit.values()) {
            if (indicatorUnit.value.equalsIgnoreCase(normalized)) {
                return indicatorUnit;
            }
        }
        String lower = normalized.toLowerCase();
        if (lower.contains("percent")) return PERCENT;
        if (lower.contains("index")) return INDEX;
        if (lower.contains("dollar") || lower.contains("currency")) return CURRENCY;
        if (lower.contains("count") || lower.contains("number") || lower.contains("persons")) return COUNT;
        if (lower.contains("volume")) return VOLUME;
        if (lower.contains("point")) return POINTS;
        return UNKNOWN;
    }
}
