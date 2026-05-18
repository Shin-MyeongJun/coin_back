package com.example.demo.contracts.message.alert;

import java.math.BigDecimal;

public record AlertFiringMessage(
        Long ruleId,
        String userId,
        String ruleLabel,
        String conditionText,
        BigDecimal observedValue,
        long firedAt
) {
    public String extractKey() {
        return userId + ":" + ruleId;
    }
}
