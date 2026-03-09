package com.example.demo.analystics.domain.service.indicator;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.indicator.close.TickCloseIndicator;
import com.example.demo.analystics.domain.domain.indicator.open.TickIndicator;
import com.example.demo.analystics.domain.service.BucketTimeAdjustService;
import com.example.demo.analystics.domain.service.ClosingData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TickIndicatorCloseService implements ClosingData<TickIndicator, TickCloseIndicator> {

    private final BucketTimeAdjustService service;

    @Override
    public TickCloseIndicator toClose(TickIndicator ind, Interval interval) {
        return new TickCloseIndicator(
                ind.getMarketCodeId(),
                interval,
                ind.getIndicatorType(),
                ind.getPeriod(),
                ind.getValue(),
                service.adjust(ind.getOpenTimestamp(), ind.getCloseTimestamp(),interval)
        );
    }
}
