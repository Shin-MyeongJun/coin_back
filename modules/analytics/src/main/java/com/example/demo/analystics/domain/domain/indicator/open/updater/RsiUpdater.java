package com.example.demo.analystics.domain.domain.indicator.open.updater;

import com.example.demo.analystics.domain.domain.indicator.open.state.IndicatorState;
import com.example.demo.analystics.domain.domain.indicator.open.state.RsiState;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RsiUpdater implements  TradeIndicatorUpdater  {

    private final int divScale =10;
    private final int n;

    private BigDecimal prev = null;      // 직전 값 (close 등)
    private BigDecimal prevSub =null;
    private BigDecimal avgGain = BigDecimal.ZERO; // 평균 상승폭
    private BigDecimal avgLoss = BigDecimal.ZERO; // 평균 하락폭
    private int initCount = 0;           // 초기화 단계에서 누적된 구간 수
    private BigDecimal sumGain = BigDecimal.ZERO; // 초기화용 누적 gain
    private BigDecimal sumLoss = BigDecimal.ZERO;// 초기화용 누적 loss

    public RsiUpdater(int n, BigDecimal prev) {
        this.n = n;
        this.prev = prev;
    }


    @Override
    public BigDecimal cal(BigDecimal value) {

        prevSub = value;

        BigDecimal delta = value.subtract(prev);
        BigDecimal gain  = delta.signum() > 0 ? delta : BigDecimal.ZERO;
        BigDecimal loss  = delta.signum() < 0 ? delta.abs() : BigDecimal.ZERO;
        if(initCount != n) {
            return BigDecimal.ZERO;
        }
        BigDecimal liveAvgGain = avgGain
                .multiply(BigDecimal.valueOf(n - 1))
                .add(gain)
                .divide(BigDecimal.valueOf(n), divScale, RoundingMode.HALF_UP);

        // avgLoss_t = ((n-1)*avgLoss_{t-1} + loss_t) / n
        BigDecimal liveAvgLoss = avgLoss
                .multiply(BigDecimal.valueOf(n - 1))
                .add(loss)
                .divide(BigDecimal.valueOf(n), divScale, RoundingMode.HALF_UP);
        return computeRsi(liveAvgGain, liveAvgLoss);
    }

    @Override
    public BigDecimal close() {

        BigDecimal val = prevSub;
        prevSub = null;

        BigDecimal delta = val.subtract(prev);
        BigDecimal gain  = delta.signum() > 0 ? delta : BigDecimal.ZERO;
        BigDecimal loss  = delta.signum() < 0 ? delta.abs() : BigDecimal.ZERO;

        if (initCount < n) {
            sumGain = sumGain.add(gain);
            sumLoss = sumLoss.add(loss);
            initCount++;
            if (initCount == n) {
                // 초깃값 확정
                avgGain = sumGain.divide(BigDecimal.valueOf(n), divScale, RoundingMode.HALF_UP);
                avgLoss = sumLoss.divide(BigDecimal.valueOf(n), divScale, RoundingMode.HALF_UP);
                prev = val;
                return computeRsi(avgGain, avgLoss);
            } else {
                return BigDecimal.ZERO;
            }
        }
        BigDecimal settled =cal(val);

        avgGain = avgGain
                .multiply(BigDecimal.valueOf(n - 1))
                .add(gain)
                .divide(BigDecimal.valueOf(n), divScale, RoundingMode.HALF_UP);
        avgLoss = avgLoss
                .multiply(BigDecimal.valueOf(n - 1))
                .add(loss)
                .divide(BigDecimal.valueOf(n), divScale, RoundingMode.HALF_UP);
        prev = val;
        return settled;
    }

    @Override
    public IndicatorState payload() {
        return new RsiState(
                prev,
                avgGain,
                avgLoss,
                initCount
        );
    }


    private BigDecimal computeRsi(BigDecimal ag, BigDecimal al) {
        // 경계 처리
        if (al.signum() == 0) return BigDecimal.valueOf(100); // 손실 없음 → RSI=100
        if (ag.signum() == 0) return BigDecimal.ZERO;         // 이득 없음 → RSI=0

        // RS = ag/al
        BigDecimal rs = ag.divide(al,  divScale, RoundingMode.HALF_UP);
        // RSI = 100 - 100/(1+RS)
        BigDecimal denom = BigDecimal.ONE.add(rs);
        BigDecimal term  = BigDecimal.valueOf(100).divide(denom,  divScale, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(100).subtract(term);
    }
}
