package com.example.demo.analystics.domain.domain.indicator.open;

import com.example.demo.analystics.domain.domain.indicator.open.updater.TradeIndicatorUpdater;
import com.example.demo.analystics.domain.domain.key.IndicatorKey;
import com.example.demo.analystics.domain.domain.key.PremiumKey;

public class PremiumIndicator extends OpenTradeIndicator<PremiumKey> {

    public PremiumIndicator(PremiumKey dataKey, IndicatorKey indicatorKey, TradeIndicatorUpdater updater) {
        super(dataKey, indicatorKey,updater);
    }

    public long getBaseExchangeId(){
        return getDataKey().baseExchangeId();
    }
    public long getCompareExchangeId(){
        return getDataKey().compareExchangeId();
    }
    public String getBase(){
        return getDataKey().base();
    }
}
