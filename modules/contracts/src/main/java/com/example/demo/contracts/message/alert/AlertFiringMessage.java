package com.example.demo.contracts.message.alert;

import java.util.Objects;

/**
 * Alert rule 발화 이벤트 Kafka 메시지(JSON record 기반).
 * <p>
 * {@code ruleType}, {@code direction}을 Java {@code enum}이 아닌 {@code String}으로
 * 둔 이유: Kafka 메시지 호환성. enum 추가/이름 변경 시 producer/consumer 간
 * 직렬화 호환이 깨지므로, 계약 단계에서는 String으로 유지하고 검증은 호출 측
 * (alert 모듈 usecase / @RestControllerAdvice)이 담당한다.
 * <p>
 * 필드 nullable 가드도 record 자체가 아닌 정적 팩토리 {@link #of}에서 수행한다.
 */
public record AlertFiringMessage(
        String ruleId,
        String accountId,
        String ruleType,
        String summary,
        double observedValue,
        double thresholdValue,
        String direction,
        long firedAt
) {

    public static AlertFiringMessage of(
            String ruleId,
            String accountId,
            String ruleType,
            String summary,
            double observedValue,
            double thresholdValue,
            String direction,
            long firedAt
    ) {
        return new AlertFiringMessage(
                Objects.requireNonNull(ruleId, "ruleId"),
                Objects.requireNonNull(accountId, "accountId"),
                Objects.requireNonNull(ruleType, "ruleType"),
                Objects.requireNonNull(summary, "summary"),
                observedValue,
                thresholdValue,
                Objects.requireNonNull(direction, "direction"),
                firedAt
        );
    }
}
