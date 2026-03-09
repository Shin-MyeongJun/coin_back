package com.example.demo.analystics.domain.buffer.indicator;


import com.example.demo.analystics.domain.domain.indicator.open.OpenTradeIndicator;
import com.example.demo.analystics.domain.domain.key.IndicatorKey;
import com.example.demo.analystics.domain.domain.key.DataKey;
import com.example.demo.analystics.domain.factory.indicator.value.IndicatorFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public abstract class IndicatorBuffer<KEY extends DataKey<KEY> , IND extends OpenTradeIndicator<KEY>> {
    private final Map<KEY,Map<IndicatorKey,IND>> buffer = new ConcurrentHashMap<>();
    private final IndicatorFactory<KEY,IND> factory;

    IndicatorBuffer(IndicatorFactory<KEY,IND> factory) {
        this.factory = factory;
    }

    public void update(KEY key, BigDecimal val) {
       if(!buffer.containsKey(key)){
           Map<IndicatorKey, IND> indicators = new ConcurrentHashMap<>(factory.createIndicators(key,val));
           buffer.put(key,indicators);
       }
       buffer.get(key).forEach(
               (indicatorKey, ind) -> {
                   ind.update(val);
               }
       );
    }

    public void open(KEY key){
        buffer.get(key).forEach(
                (indicatorKey, ind) -> {
                    ind.open();
                }
        );
    }


    public void close() {
        buffer.values().stream()
                .toList()
                .forEach((map)->{
                    map.values().stream().toList().forEach(
                            OpenTradeIndicator::close
                    );
                });
    }


    public List<IND> getAll(){
        return buffer.values()
                .stream()
                .map(val->{
                    return val.values().stream().findFirst().get();
                })
                .toList();

    }
    
    public List<IND> drain(){
        close();
        List<IND> result = new ArrayList<>();
        buffer.forEach((key,val)->{
            result.addAll(val.values());
            open(key);
        });
        return result;
    }


}
