package com.example.demo.analystics.domain.buffer.candle;


import com.example.demo.analystics.domain.domain.candle.open.OpenCandle;
import com.example.demo.analystics.domain.domain.key.DataKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CandleBuffer
        <
                KEY extends DataKey<KEY>,
                VAL extends Comparable<VAL>,
                CANDLE extends OpenCandle<KEY,VAL>>
{
    private final Map<KEY,CANDLE>  buffer;


    public CandleBuffer() {
        buffer = new HashMap<>();
    }


    public void update(KEY key, VAL val) {
            CANDLE candle = buffer.computeIfAbsent(key, k -> createCandle(k, val));
            candle.update(val);
    }

    public void assign(List<CANDLE> list){
       buffer.clear();
       list.forEach(candle -> buffer.put(candle.getKey(), candle));
    }

    public List<CANDLE> getCandles() {
        return new ArrayList<>(buffer.values());
    }

    public synchronized List<CANDLE> drain() {
        close();
        List<CANDLE> result = new ArrayList<>(buffer.values());
        buffer.clear();
        return result;
    }

    public int size() {
        return buffer.size();
    }

    public boolean isEmpty() {
        return buffer.isEmpty();
    }

    private void close(){
       buffer.values().forEach(OpenCandle::close);
    }

    protected abstract CANDLE createCandle(KEY key,VAL val);

}
