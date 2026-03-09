package com.example.demo.analystics.domain.manager.candle;


import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.buffer.candle.CandleBuffer;
import com.example.demo.analystics.domain.domain.candle.open.OpenCandle;
import com.example.demo.analystics.domain.domain.key.DataKey;
import com.example.demo.analystics.domain.service.ClosingData;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public  abstract class  CandleManager
        <
                KEY extends DataKey<KEY>,
                VAL extends Comparable<VAL>,
                CANDLE extends OpenCandle<KEY,VAL>,
                CLOSE_CANDLE,
                BUFFER extends CandleBuffer<KEY,VAL, CANDLE>
                > {
    protected final Map<Interval,BUFFER> buffers = new ConcurrentHashMap<>();
    private final ClosingData<CANDLE,CLOSE_CANDLE> closeService;

    CandleManager(ClosingData<CANDLE, CLOSE_CANDLE> closeService){
        this.closeService = closeService;
        buffers.put(Interval.M1,create());
        buffers.put(Interval.M3,create());
        buffers.put(Interval.M5,create());
        buffers.put(Interval.M15,create());
        buffers.put(Interval.M30,create());
        buffers.put(Interval.M60,create());
        buffers.put(Interval.M240,create());
    }


    public void insert(KEY key,VAL val){
        for(BUFFER buffer : buffers.values()){
           buffer.update(key,val);
        }
    }

    public void assign(Map<Interval,List<CANDLE>> candles){
        candles.forEach((key,list)->{
            buffers.get(key).assign(list);
        });
    }

    public List<CANDLE> getCandles(Interval interval) {
        return buffers.get(interval).getCandles();
    }

    public List<CLOSE_CANDLE> drain(Interval interval) {
        return buffers.get(interval).drain().stream()
                .map(candle -> closeService.toClose(candle,interval))
                .toList();
    }

    public int size(Interval interval) {
        return buffers.get(interval).size();
    }

    public boolean isEmpty(Interval interval) {
        return buffers.get(interval).isEmpty();
    }

    protected abstract  BUFFER create();


}
