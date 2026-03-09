package com.example.demo.analystics.domain.service.candle;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.close.PremiumCloseCandle;
import com.example.demo.analystics.domain.domain.candle.open.PremiumCandle;
import com.example.demo.analystics.domain.service.BucketTimeAdjustService;
import com.example.demo.analystics.domain.service.ClosingData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PremiumCandleCloseService implements ClosingData<PremiumCandle,PremiumCloseCandle> {

    private final BucketTimeAdjustService service;

    @Override
    public PremiumCloseCandle toClose(PremiumCandle c , Interval interval) {
        return new PremiumCloseCandle(
                c.getBase(),
                c.getBaseExchangeId(),
                c.getCompareExchangeId(),
                interval,
                c.getOpen(),
                c.getHigh(),
                c.getLow(),
                c.getClose(),
                service.adjust(c.getOpenTimestamp(), c.getCloseTimestamp(),interval)
        );
    }
}
