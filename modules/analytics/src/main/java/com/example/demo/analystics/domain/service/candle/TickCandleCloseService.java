package com.example.demo.analystics.domain.service.candle;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.close.TickCloseCandle;
import com.example.demo.analystics.domain.domain.candle.open.TickCandle;
import com.example.demo.analystics.domain.service.BucketTimeAdjustService;
import com.example.demo.analystics.domain.service.ClosingData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TickCandleCloseService implements ClosingData<TickCandle,TickCloseCandle> {

    private final BucketTimeAdjustService service;

    @Override
    public TickCloseCandle toClose(TickCandle c , Interval interval) {
        return new TickCloseCandle(
                c.getMarketCodeId(),
                interval,
                c.getOpen(),
                c.getHigh(),
                c.getLow(),
                c.getClose(),
                service.adjust(c.getOpenTimestamp(), c.getCloseTimestamp(),interval)
        );
    }
}
