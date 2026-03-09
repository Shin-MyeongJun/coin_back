package com.example.demo.analystics.domain.manager.candle;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.buffer.candle.TickCandleBuffer;
import com.example.demo.analystics.domain.domain.candle.close.TickCloseCandle;
import com.example.demo.analystics.domain.domain.candle.open.TickCandle;
import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.analystics.domain.service.ClosingData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TickCandleManager extends CandleManager<TickKey, BigDecimal, TickCandle, TickCloseCandle, TickCandleBuffer> {
    public TickCandleManager(ClosingData<TickCandle,TickCloseCandle> closeService) {
        super(closeService);
        for (Interval interval : Interval.values()) {
            buffers.put(interval, new TickCandleBuffer());
        }
    }

    @Override
    protected TickCandleBuffer create() {
        return new TickCandleBuffer();
    }
}
