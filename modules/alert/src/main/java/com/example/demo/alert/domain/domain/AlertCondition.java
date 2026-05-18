package com.example.demo.alert.domain.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record AlertCondition(
        Operator operator,
        BigDecimal threshold
) {
    public AlertCondition {
        Objects.requireNonNull(operator, "operator must not be null");
        Objects.requireNonNull(threshold, "threshold must not be null");
    }

    public boolean matches(BigDecimal observedValue) {
        Objects.requireNonNull(observedValue, "observedValue must not be null");
        return operator.matches(observedValue, threshold);
    }

    public String asText() {
        return operator.symbol() + " " + threshold.stripTrailingZeros().toPlainString();
    }
}
