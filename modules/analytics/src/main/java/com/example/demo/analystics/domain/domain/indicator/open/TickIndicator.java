package com.example.demo.analystics.domain.domain.indicator.open;

import com.example.demo.analystics.domain.domain.indicator.open.updater.TradeIndicatorUpdater;
import com.example.demo.analystics.domain.domain.key.IndicatorKey;
import com.example.demo.analystics.domain.domain.key.TickKey;

public class TickIndicator extends OpenTradeIndicator<TickKey>{
    public TickIndicator(TickKey dataKey, IndicatorKey indicatorKey, TradeIndicatorUpdater updater) {
        super(dataKey, indicatorKey ,updater);
    }

    public Long getMarketCodeId(){
        return getDataKey().MarketCodeId();
    }
}
