package com.example.demo.analystics.domain.factory.indicator.value;

import com.example.demo.analystics.domain.domain.indicator.open.PremiumIndicator;
import com.example.demo.analystics.domain.domain.key.IndicatorKey;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PremiumIndicatorFactory extends IndicatorFactory<PremiumKey,PremiumIndicator> {

    @Override
    protected PremiumIndicator createIndicator(PremiumKey dataKey, IndicatorKey indicatorKey) {
        return new PremiumIndicator(
                dataKey,
                indicatorKey,
                createUpdater(indicatorKey)
        );
    }

    @Override
    protected PremiumIndicator createIndicator(PremiumKey dataKey, IndicatorKey indicatorKey, BigDecimal initVal) {
        return new PremiumIndicator(
                dataKey,
                indicatorKey,
                createUpdater(indicatorKey,initVal)
        );
    }

}
