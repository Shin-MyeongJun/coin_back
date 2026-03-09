package com.example.demo.analystics.domain.manager.candle;


import com.example.demo.analystics.domain.buffer.candle.PremiumDetailCandleBuffer;
import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.close.PremiumDetailCloseCandle;
import com.example.demo.analystics.domain.domain.candle.open.PremiumDetailCandle;
import com.example.demo.analystics.domain.domain.candle.value.PremiumDetailValue;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.service.ClosingData;
import org.springframework.stereotype.Component;

@Component
public class PremiumDetailCandleManager extends CandleManager<PremiumKey, PremiumDetailValue, PremiumDetailCandle, PremiumDetailCloseCandle, PremiumDetailCandleBuffer> {
    public PremiumDetailCandleManager(ClosingData<PremiumDetailCandle, PremiumDetailCloseCandle> closeService) {
        super(closeService);
        for (Interval interval : Interval.values()) {
            buffers.put(interval, new PremiumDetailCandleBuffer());
        }
    }

    @Override
    protected PremiumDetailCandleBuffer create() {
        return new PremiumDetailCandleBuffer();
    }
}
