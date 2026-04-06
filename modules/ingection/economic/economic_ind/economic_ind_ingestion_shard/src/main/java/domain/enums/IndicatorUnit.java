package domain.enums;

public enum IndicatorUnit {
    PERCENT("PERCENT"),
    INDEX("PERCENT"),
    CURRENCY("PERCENT"),
    COUNT("PERCENT"),
    VOLUME("PERCENT"),
    POINTS("PERCENT"),
    UNKNOWN("PERCENT");

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
