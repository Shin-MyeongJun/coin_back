package com.example.demo.analystics.domain.domain.indicator.open.updater;

import com.example.demo.analystics.domain.domain.indicator.open.state.EmaState;
import com.example.demo.analystics.domain.domain.indicator.open.state.IndicatorState;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

public class EmaUpdater implements  TradeIndicatorUpdater {

    private static final MathContext MC = new MathContext(12, RoundingMode.HALF_UP);

    private final int period;
    private final BigDecimal alpha;
    private  BigDecimal prev;
    private  BigDecimal prevSub;

    public EmaUpdater(int period) {
        if (period <= 0) {
            throw new IllegalArgumentException("period must be > 0");
        }
        this.period = period;
        // alpha = 2 / (period + 1)
        this.alpha = BigDecimal.valueOf(2).divide(  BigDecimal.ONE.add(BigDecimal.valueOf(period)) , MC);
    }

    public EmaUpdater(int period, BigDecimal prev) {
        if (period <= 0) {
            throw new IllegalArgumentException("period must be > 0");
        }
        this.period = period;
        // alpha = 2 / (period + 1)
        this.alpha = BigDecimal.valueOf(2).divide(  BigDecimal.ONE.add(BigDecimal.valueOf(period)) , MC);
        this.prev = prev;
    }

    public EmaUpdater(int period, EmaState state) {
        this(period, state.prev());
    }



    @Override
    public BigDecimal cal(BigDecimal val) {
        Objects.requireNonNull(val, "value");
        if (prev == null) {           // 초기 EMA
            prev = val;
            return prev;
        }
        prevSub = val;
        BigDecimal oneMinusAlpha = BigDecimal.ONE.subtract(alpha, MC);
        return val.multiply(alpha, MC)
                .add(prev.multiply(oneMinusAlpha, MC), MC);
    }

    @Override
    public BigDecimal close() {
        BigDecimal settled = cal(prevSub);
        prev = settled;
        return settled ;
    }

    @Override
    public IndicatorState payload() {
        return new EmaState(
                prev
        );
    }


}
