package com.example.demo.analystics.domain.domain.indicator.open.state;

import java.math.BigDecimal;

public record EmaState(
        BigDecimal prev
)implements IndicatorState {
}
