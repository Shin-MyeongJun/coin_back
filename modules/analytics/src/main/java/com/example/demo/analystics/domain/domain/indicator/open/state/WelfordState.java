package com.example.demo.analystics.domain.domain.indicator.open.state;

import java.math.BigDecimal;

public record WelfordState(
        BigDecimal mean,
        BigDecimal count
) implements IndicatorState {
}
