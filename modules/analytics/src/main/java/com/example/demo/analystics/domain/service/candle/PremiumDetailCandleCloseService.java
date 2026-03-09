package com.example.demo.analystics.domain.service.candle;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.close.PremiumDetailCloseCandle;
import com.example.demo.analystics.domain.domain.candle.open.PremiumDetailCandle;
import com.example.demo.analystics.domain.service.BucketTimeAdjustService;
import com.example.demo.analystics.domain.service.ClosingData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PremiumDetailCandleCloseService
        implements ClosingData< PremiumDetailCandle,
                                PremiumDetailCloseCandle
        >
{

    private final BucketTimeAdjustService service;

    @Override
    public PremiumDetailCloseCandle toClose(PremiumDetailCandle c , Interval interval) {
        return new PremiumDetailCloseCandle(
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
