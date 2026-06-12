package com.example.demo.alert.domain.exception;

/**
 * Alert rule 미조회 예외.
 * rule 미존재와 타 사용자 소유를 의도적으로 구분하지 않는다 (존재 여부 비노출 — 404 단일 응답).
 */
public class AlertRuleNotFoundException extends RuntimeException {

    public AlertRuleNotFoundException(long ruleId) {
        super("alert rule not found: " + ruleId);
    }
}
