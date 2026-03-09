package com.example.demo.analystics.domain.domain.indicator.open.state;

import java.math.BigDecimal;

public record TrState(
   BigDecimal high,
   BigDecimal low,
   BigDecimal prevClose
) implements IndicatorState {
}
