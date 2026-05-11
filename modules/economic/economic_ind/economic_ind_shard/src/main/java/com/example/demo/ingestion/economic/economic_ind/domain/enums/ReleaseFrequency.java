package com.example.demo.ingestion.economic.economic_ind.domain.enums;

public enum ReleaseFrequency {
    WEEKLY("WEEKLY"),               // 매주 (예: 신규 실업수당 청구 건수)
    MONTHLY("MONTHLY"),             // 매월 (예: CPI, 고용보고서)
    QUARTERLY("QUARTERLY"),         // 매분기 (예: GDP)
    YEARLY("YEARLY"),               // 매년
    SEMI_ANNUALLY("SEMI_ANNUALLY"),
    PERIODIC("PERIODIC"),           // fomc 등 정해진 일정들
    IRREGULAR("IRREGULAR"),         // 불규칙 (예: 연준 의장 연설 등 이벤트성)
    UNKNOWN("UNKNOWN");             // 알 수 없음 (매칭 실패 시 기본값)

    private String value;

    ReleaseFrequency(String value) {
        this.value = value;
    }

    public static ReleaseFrequency fromValue(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }
        for (ReleaseFrequency frequency : ReleaseFrequency.values()) {
            if (frequency.value.equalsIgnoreCase(value)) {
                return frequency;
            }
        }
        return UNKNOWN;
    }
}
