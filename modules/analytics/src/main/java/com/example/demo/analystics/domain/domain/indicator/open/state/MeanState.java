package com.example.demo.analystics.domain.domain.indicator.open.state;

import java.math.BigDecimal;

public record MeanState(
        BigDecimal count,
        BigDecimal sum
) implements IndicatorState {
}
