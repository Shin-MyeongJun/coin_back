package com.example.demo.analystics.domain.domain.indicator.open.updater;

import com.example.demo.analystics.domain.domain.indicator.open.state.IndicatorState;
import com.example.demo.analystics.domain.domain.indicator.open.state.TrState;

import java.math.BigDecimal;
import java.util.Objects;

public class TrUpdater implements TradeIndicatorUpdater {
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal prevClose;
    private BigDecimal prevSub;

    public  TrUpdater(){

    }
    public TrUpdater(BigDecimal high, BigDecimal low, BigDecimal prevClose) {
        this.high = high;
        this.low = low;
        this.prevClose = prevClose;
    }

    public  TrUpdater(BigDecimal val) {
        this.high = val;
        this.low = val;
        this.prevClose = val;
    }

    public TrUpdater(TrState state) {
        this(state.high(), state.low(), state.prevClose());
    }


    @Override
    public BigDecimal cal(BigDecimal value) {
        Objects.requireNonNull(value);
        prevSub = value;

        // 고가/저가 갱신
        if (high == null || value.compareTo(high) > 0) high = value;   // value가 더 크면 high 갱신
        if (low == null || value.compareTo(low)  < 0) low  = value;   // value가 더 작으면 low 갱신

        // True Range 계산: max( high - low, |high - prevClose|, |low - prevClose| )
        BigDecimal r1 = high.subtract(low).abs();
        BigDecimal r2 = high.subtract(prevClose).abs();
        BigDecimal r3 = low.subtract(prevClose).abs();

        return r1.max(r2).max(r3);
    }

    @Override
    public BigDecimal close() {
        BigDecimal settled = cal(prevSub);
        prevClose = prevSub;
        return settled;
    }

    @Override
    public IndicatorState payload() {
        return new TrState(
                high,
                low,
                prevClose
        );
    }
}
