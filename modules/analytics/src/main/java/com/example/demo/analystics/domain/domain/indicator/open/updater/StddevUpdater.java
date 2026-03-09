package com.example.demo.analystics.domain.domain.indicator.open.updater;

import com.example.demo.analystics.domain.domain.indicator.open.state.IndicatorState;
import com.example.demo.analystics.domain.domain.indicator.open.state.WelfordState;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class StddevUpdater implements TradeIndicatorUpdater {

    private BigDecimal count  = BigDecimal.ZERO;
    private BigDecimal mean  = BigDecimal.ZERO;
    private BigDecimal m2 = BigDecimal.ZERO;


    @Override
    public BigDecimal close() {
        BigDecimal settled = BigDecimal.ZERO;
        if(count.compareTo(BigDecimal.TWO) > 0){
            BigDecimal variance = m2.divide(count,10,RoundingMode.HALF_UP);
            settled = variance.sqrt(MathContext.DECIMAL32);
        }
        count = BigDecimal.ZERO;
        mean = BigDecimal.ZERO;
        m2 = BigDecimal.ZERO;
        return settled;
    }

    @Override
    public IndicatorState payload() {
        return new WelfordState(
                mean,
                count
        );
    }

    @Override
    public BigDecimal cal(BigDecimal value) {
        count = count.add(BigDecimal.ONE);
        BigDecimal delta  = value.subtract(mean);
        mean         =mean.add(delta.divide(count,10, RoundingMode.HALF_UP));
        BigDecimal delta2 = value.subtract(mean);
        m2 =m2.add(delta.multiply(delta2));

        BigDecimal variance = m2.divide(count,10,RoundingMode.HALF_UP);

        return variance.sqrt(MathContext.DECIMAL32);
    }
}
