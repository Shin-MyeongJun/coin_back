package com.example.demo.analystics.domain.domain.indicator.open.state;

import java.math.BigDecimal;

public record RsiState(
        BigDecimal prevCloseScaled,
        BigDecimal avgGainScaled,
        BigDecimal avgLossScaled,
        int initCount,
        BigDecimal sumGainScaled,
        BigDecimal sumLossScaled
)implements IndicatorState{}
