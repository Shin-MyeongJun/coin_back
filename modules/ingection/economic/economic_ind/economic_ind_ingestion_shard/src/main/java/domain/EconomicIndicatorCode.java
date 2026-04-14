package domain;

import domain.enums.IndicatorUnit;
import domain.enums.ReleaseFrequency;

public record EconomicIndicatorCode(
        String indicatorCode,
        String country,
        String type, //데이터 소스
        ReleaseFrequency frequency,     // "Monthly"
        IndicatorUnit unit //단위
) {
}
