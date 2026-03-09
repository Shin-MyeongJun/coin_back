package com.example.demo.analystics.domain.domain.indicator.open.updater;

import com.example.demo.analystics.domain.domain.indicator.open.state.IndicatorState;
import com.example.demo.analystics.domain.domain.indicator.open.state.WelfordState;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MeanUpdater implements TradeIndicatorUpdater {
    private BigDecimal sum = BigDecimal.ZERO;
    private BigDecimal count = BigDecimal.ZERO;





    @Override
    public BigDecimal cal(BigDecimal val) {
        count = count.add(BigDecimal.ONE);
        sum = sum.add(val);
        return sum.divide(count, 12, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal close() {
        if(sum.compareTo(BigDecimal.ZERO) == 0 || count.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal settled = sum.divide(count, 12, RoundingMode.HALF_UP);
        sum = BigDecimal.ZERO;
        count = BigDecimal.ZERO;
        return settled;
    }

    @Override
    public IndicatorState payload() {
        return new WelfordState(
                sum.divide(count, 12, RoundingMode.HALF_UP),
                count
        );
    }
}
