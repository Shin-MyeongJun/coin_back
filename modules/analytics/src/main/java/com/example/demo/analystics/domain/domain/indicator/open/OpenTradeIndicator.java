package com.example.demo.analystics.domain.domain.indicator.open;

import com.example.demo.analystics.domain.domain.indicator.TradeIndicatorType;
import com.example.demo.analystics.domain.domain.indicator.open.state.IndicatorState;
import com.example.demo.analystics.domain.domain.indicator.open.updater.TradeIndicatorUpdater;
import com.example.demo.analystics.domain.domain.key.IndicatorKey;
import com.example.demo.analystics.domain.domain.key.DataKey;

import java.math.BigDecimal;

public abstract class OpenTradeIndicator<KEY extends DataKey<KEY>> {
    private final KEY dataKey;
    private final IndicatorKey indicatorKey;
    private final TradeIndicatorUpdater updater;
    private BigDecimal value;
    private Long openTimestamp;
    private Long closeTimestamp;
    private Boolean isClosed = false;

    public OpenTradeIndicator(KEY dataKey,IndicatorKey indicatorKey ,TradeIndicatorUpdater updater) {
        this.dataKey = dataKey;
        this.indicatorKey = indicatorKey;
        this.updater = updater;
        this.openTimestamp = System.currentTimeMillis();
    }

    public void update(BigDecimal value) {
        if(isClosed) return;
        this.value = updater.cal(value);
    }

    public void close() {
        this.value = updater.close();
        closeTimestamp = System.currentTimeMillis();
        isClosed = true;
    }

    public void open() {
        isClosed = false;
        openTimestamp = System.currentTimeMillis();
    }

    public KEY getDataKey(){
        return dataKey;
    }
    public IndicatorKey  getIndicatorKey(){return indicatorKey;}
    public IndicatorState getPayload() { return  updater.payload();}
    public int getPeriod(){
       return indicatorKey.period();
    }
    public TradeIndicatorType getIndicatorType(){
        return indicatorKey.type();
    }

    public BigDecimal getValue(){
        return value;
    }
    public Long getOpenTimestamp(){return openTimestamp;}
    public Long getCloseTimestamp(){return closeTimestamp;}




}
