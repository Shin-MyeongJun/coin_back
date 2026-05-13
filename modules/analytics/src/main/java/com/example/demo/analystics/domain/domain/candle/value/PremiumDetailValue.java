package com.example.demo.analystics.domain.domain.candle.value;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record PremiumDetailValue(
        BigDecimal baseVal,
        BigDecimal baseQuoteVal,

        BigDecimal compareVal,
        BigDecimal compareQuoteVal
) implements Comparable<PremiumDetailValue> {


    @Override
    public int compareTo(PremiumDetailValue o) {
        return  hedgeVal().compareTo(o.hedgeVal());
    }

    private BigDecimal hedgeVal(){
        return compareVal.divide(baseVal, 8, RoundingMode.HALF_DOWN);
    }
}
