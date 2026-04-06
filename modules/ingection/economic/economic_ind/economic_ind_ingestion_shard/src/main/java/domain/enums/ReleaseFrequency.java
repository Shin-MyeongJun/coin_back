package domain.enums;

public enum ReleaseFrequency {
    WEEKLY,     // 매주 (예: 신규 실업수당 청구 건수)
    MONTHLY,    // 매월 (예: CPI, 고용보고서)
    QUARTERLY,  // 매분기 (예: GDP)
    YEARLY,     // 매년
    SEMI_ANNUALLY,

    PERIODIC, //fomc 등  정해진 일정들
    IRREGULAR;  // 불규칙 (예: 연준 의장 연설 등 이벤트성)
}
