package com.example.demo.analystics.domain.domain.indicator.open.state;

import java.math.BigDecimal;

public  record StddevState(
        BigDecimal count,
        BigDecimal mean,
        BigDecimal m2
) implements IndicatorState {
}