package com.example.demo.analystics.domain.manager.candle;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.buffer.candle.PremiumCandleBuffer;
import com.example.demo.analystics.domain.domain.candle.close.PremiumCloseCandle;
import com.example.demo.analystics.domain.domain.candle.open.PremiumCandle;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.service.ClosingData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PremiumCandleManager extends CandleManager<PremiumKey, BigDecimal, PremiumCandle, PremiumCloseCandle, PremiumCandleBuffer> {
    public PremiumCandleManager(ClosingData<PremiumCandle,PremiumCloseCandle> closeService) {
        super(closeService);
        for (Interval interval : Interval.values()) {
            buffers.put(interval, new PremiumCandleBuffer());
        }
    }

    @Override
    protected PremiumCandleBuffer create() {
        return new PremiumCandleBuffer();
    }
}
