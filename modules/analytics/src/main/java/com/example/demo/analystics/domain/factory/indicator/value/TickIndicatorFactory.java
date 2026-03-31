package com.example.demo.analystics.domain.factory.indicator.value;

import com.example.demo.analystics.domain.domain.indicator.open.TickIndicator;
import com.example.demo.analystics.domain.domain.indicator.open.state.IndicatorState;
import com.example.demo.analystics.domain.domain.key.IndicatorKey;
import com.example.demo.analystics.domain.domain.key.TickKey;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TickIndicatorFactory extends IndicatorFactory<TickKey,TickIndicator> {

    @Override
    protected TickIndicator createIndicator(TickKey dataKey, IndicatorKey indicatorKey) {
        return new TickIndicator(
                dataKey,
                indicatorKey,
                createUpdater(indicatorKey)
        );
    }

    @Override
    protected TickIndicator createIndicator(TickKey dataKey, IndicatorKey indicatorKey, BigDecimal initVal) {
        return new TickIndicator(
                dataKey,
                indicatorKey,
                createUpdater(indicatorKey,initVal)
        );
    }

    @Override
    protected TickIndicator createIndicator(TickKey dataKey, IndicatorKey indicatorKey, IndicatorState state) {
        return new TickIndicator(
                dataKey,
                indicatorKey,
                createUpdater(indicatorKey,state)
        );
    }
}
