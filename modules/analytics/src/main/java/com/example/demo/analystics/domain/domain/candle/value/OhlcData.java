package com.example.demo.analystics.domain.domain.candle.value;

public class OhlcData<VAL extends Comparable<? super VAL>> {
    private VAL open;
    private VAL high;
    private VAL low;
    private VAL close;

    public OhlcData (VAL val) {
        open = val;
        high = val;
        low = val;
        close = val;
    }

    public OhlcData (VAL open, VAL high, VAL low, VAL close) {
        this.open =  open;
        this.high = high;
        this.low = low;
        this.close = close;
    }



    public VAL open(){return open;}
    public VAL close(){return close;}
    public VAL high(){return high;}
    public VAL low(){return low;}



    public void setHighLow(VAL val){
        if(compare(high,val) > 0){
            high = val;
        }
        if(compare(low,val) < 0) {
            low = val;
        }
    }

    public void setClose(VAL val){
        this.close = val;
    }



    private int compare(VAL base,VAL compare){
        return base.compareTo(compare);
    }
}
