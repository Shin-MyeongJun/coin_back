package com.example.demo.analystics.domain.domain.indicator.open.updater;

import com.example.demo.analystics.domain.domain.indicator.open.state.IndicatorState;

import java.math.BigDecimal;

public interface TradeIndicatorUpdater {
    BigDecimal cal(BigDecimal val);
    BigDecimal close();
    IndicatorState payload();
}
