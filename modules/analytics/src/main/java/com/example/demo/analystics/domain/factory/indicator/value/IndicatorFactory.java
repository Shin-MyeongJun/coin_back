package com.example.demo.analystics.domain.factory.indicator.value;

import com.example.demo.analystics.domain.domain.indicator.open.OpenTradeIndicator;
import com.example.demo.analystics.domain.domain.indicator.TradeIndicatorType;
import com.example.demo.analystics.domain.domain.indicator.open.updater.*;
import com.example.demo.analystics.domain.domain.key.IndicatorKey;
import com.example.demo.analystics.domain.domain.key.DataKey;

import java.math.BigDecimal;
import java.util.*;


public abstract class IndicatorFactory<
        KEY extends DataKey<KEY>,
        IND extends OpenTradeIndicator<KEY> > {
    private final Set<IndicatorKey> keys = new HashSet<>();
    //Db관련 추가 여부 고려 db 추가시 application 으로


    IndicatorFactory(){
        register(new IndicatorKey( 0, TradeIndicatorType.MEAN));
        register(new IndicatorKey( 0, TradeIndicatorType.STDDEV));
        register(new IndicatorKey( 0, TradeIndicatorType.TR));

        register(new IndicatorKey( 12, TradeIndicatorType.EMA));
        register(new IndicatorKey( 26, TradeIndicatorType.EMA));
        register(new IndicatorKey( 9, TradeIndicatorType.EMA));

        register(new IndicatorKey(14 , TradeIndicatorType.RSI));
        register(new IndicatorKey( 9, TradeIndicatorType.RSI));
    }

    private void register(IndicatorKey key) {
        keys.add(key);
    }

    public Map<IndicatorKey ,IND > createIndicators(KEY key){
        Map<IndicatorKey ,IND> map = new HashMap<>();
        keys.forEach(k->{
           map.put(k,createIndicator(key,k));
        });
        return map;
    }
    public Map<IndicatorKey ,IND > createIndicators(KEY key, BigDecimal initVal){
        Map<IndicatorKey ,IND> map = new HashMap<>();
        keys.forEach(k->{
            map.put(k,createIndicator(key,k,initVal));
        });
        return map;
    }

    protected abstract IND createIndicator( KEY dataKey, IndicatorKey indicatorKey);
    protected abstract IND createIndicator(KEY dataKey, IndicatorKey indicatorKey, BigDecimal initVal);

    protected TradeIndicatorUpdater createUpdater(IndicatorKey key){
        if(key.type() == TradeIndicatorType.RSI){
            return new RsiUpdater(key.period(),null);
        }
        if(key.type() == TradeIndicatorType.EMA){
            return new EmaUpdater(key.period(),null);
        }
        if(key.type() == TradeIndicatorType.MEAN){
            return new MeanUpdater();
        }
        if(key.type() == TradeIndicatorType.STDDEV){
            return new StddevUpdater();
        }
        if(key.type() == TradeIndicatorType.TR){
            return new TrUpdater(null);
        }
        return null;
    }

    protected TradeIndicatorUpdater createUpdater(IndicatorKey key,BigDecimal initVal){
        if(key.type() == TradeIndicatorType.RSI){
            return new RsiUpdater(key.period(),initVal);
        }
        if(key.type() == TradeIndicatorType.EMA){
            return new EmaUpdater(key.period(),initVal);
        }
        if(key.type() == TradeIndicatorType.MEAN){
            return new MeanUpdater();
        }
        if(key.type() == TradeIndicatorType.STDDEV){
            return new StddevUpdater();
        }
        if(key.type() == TradeIndicatorType.TR){
            return new TrUpdater(initVal);
        }
        return null;
    }
}
