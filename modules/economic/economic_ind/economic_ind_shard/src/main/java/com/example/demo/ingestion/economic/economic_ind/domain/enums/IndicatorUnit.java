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
        for (IndicatorUnit indicatorUnit : IndicatorUnit.values()) {
            if (indicatorUnit.value.equalsIgnoreCase(value)) {
                return indicatorUnit;
            }
        }
        return UNKNOWN;
    }
}
