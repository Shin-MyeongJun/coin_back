package com.example.demo.alert.domain.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record AlertFiring(
        Long id,
        Long ruleId,
        String userId,
        String ruleLabel,
        String conditionText,
        BigDecimal observedValue,
        long firedAt
) {
    public AlertFiring {
        Objects.requireNonNull(ruleId, "ruleId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(ruleLabel, "ruleLabel must not be null");
        Objects.requireNonNull(conditionText, "conditionText must not be null");
        Objects.requireNonNull(observedValue, "observedValue must not be null");
    }
}
