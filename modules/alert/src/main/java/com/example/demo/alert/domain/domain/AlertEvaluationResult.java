package com.example.demo.alert.domain.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record AlertEvaluationResult(
        boolean matched,
        AlertMetric metric,
        BigDecimal observedValue,
        BigDecimal threshold
) {
    public AlertEvaluationResult {
        Objects.requireNonNull(metric, "metric must not be null");
        Objects.requireNonNull(observedValue, "observedValue must not be null");
        Objects.requireNonNull(threshold, "threshold must not be null");
    }
}
