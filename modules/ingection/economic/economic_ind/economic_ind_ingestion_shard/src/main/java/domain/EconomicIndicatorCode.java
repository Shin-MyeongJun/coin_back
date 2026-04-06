package domain;

public record EconomicIndicatorCode(
        String indicatorCode,
        String country,
        String type, //데이터 소스
        String frequency,     // "Monthly"
        String unit //단위
) {
}
