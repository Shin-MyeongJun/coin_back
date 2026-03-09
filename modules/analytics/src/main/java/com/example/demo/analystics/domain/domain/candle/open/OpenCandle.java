package com.example.demo.analystics.domain.domain.candle.open;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.value.OhlcData;
import com.example.demo.analystics.domain.domain.key.DataKey;



public abstract class OpenCandle<
        KEY extends DataKey<KEY>,
        VAL extends Comparable<VAL>
        > {
   private final KEY key;
   private final OhlcData<VAL> ohlcData;
   private final Interval interval;
   private final long openTimestamp;
    private long closeTimestamp;
   private Boolean isClosed = false;

   public OpenCandle(KEY key, VAL val,Interval interval) {
       this.key = key;
       this.ohlcData = new OhlcData<>(val);
       this.interval = interval;
       this.openTimestamp = System.currentTimeMillis();
   }

    public OpenCandle(KEY key,OhlcData<VAL> ohlcData,Interval interval,long openTimestamp) {
        this.key = key;
        this.ohlcData = ohlcData;
        this.interval = interval;
        this.openTimestamp = openTimestamp;
    }

   public Boolean isSame(KEY key){
       return this.key.equals(key);
   }

    public void update(VAL val) {
        if(isClosed) return;
        ohlcData.setHighLow(val);
    }

    public void close() {
        if(isClosed) return;
        closeTimestamp = System.currentTimeMillis();
        isClosed = true;
    }



    public VAL getOpen(){
       return ohlcData.open();
    }
    public VAL getHigh(){
        return ohlcData.high();
    }
    public VAL getLow(){
        return ohlcData.low();
    }
    public VAL getClose(){
        return ohlcData.close();
    }
    public  Long getOpenTimestamp(){return openTimestamp;}
    public  Long getCloseTimestamp(){return closeTimestamp;}

    public KEY getKey(){
        return key;
    }
    public Interval getInterval(){return interval;};

}
