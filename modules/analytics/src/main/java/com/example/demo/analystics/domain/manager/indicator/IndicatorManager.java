package com.example.demo.analystics.domain.manager.indicator;


import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.buffer.indicator.IndicatorBuffer;
import com.example.demo.analystics.domain.factory.indicator.buffer.IndicatorBufferFactory;
import com.example.demo.analystics.domain.domain.indicator.open.OpenTradeIndicator;
import com.example.demo.analystics.domain.domain.key.DataKey;
import com.example.demo.analystics.domain.service.ClosingData;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class IndicatorManager<
        KEY extends DataKey<KEY>,
        IND extends OpenTradeIndicator<KEY>,
        CLOSE_IND
                > {
    private final Map<Interval,IndicatorBuffer<KEY,IND>> buffers = new ConcurrentHashMap<>();
    protected final IndicatorBufferFactory<KEY,IND> factory;
    private final ClosingData<IND ,CLOSE_IND> closeService;

    IndicatorManager(IndicatorBufferFactory<KEY,IND> factory, ClosingData<IND , CLOSE_IND> closeService){
        this.factory = factory;
        this.closeService = closeService;
        buffers.put(Interval.M1,factory.create());
        buffers.put(Interval.M3,factory.create());
        buffers.put(Interval.M5,factory.create());
        buffers.put(Interval.M15,factory.create());
        buffers.put(Interval.M30,factory.create());
        buffers.put(Interval.M60,factory.create());
        buffers.put(Interval.M240,factory.create());
    }

    public void update(KEY key, BigDecimal val){
        for(IndicatorBuffer<KEY,IND>  buffer : buffers.values()){
            buffer.update(key,val);
        }
    }

    public List<IND> getInds(Interval interval) {
        buffers.get(interval).close();
        return buffers.get(interval).getAll();
    }

    public List<CLOSE_IND> drain(Interval interval){
       return  buffers.get(interval).drain().stream()
               .map(val->closeService.toClose(val,interval))
               .toList();
    }
}
