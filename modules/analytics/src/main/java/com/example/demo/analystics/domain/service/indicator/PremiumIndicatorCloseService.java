package com.example.demo.analystics.domain.service.indicator;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.indicator.close.PremiumCloseIndicator;
import com.example.demo.analystics.domain.domain.indicator.open.PremiumIndicator;
import com.example.demo.analystics.domain.service.BucketTimeAdjustService;
import com.example.demo.analystics.domain.service.ClosingData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PremiumIndicatorCloseService implements ClosingData<PremiumIndicator, PremiumCloseIndicator> {

    private final BucketTimeAdjustService service;

    @Override
    public PremiumCloseIndicator toClose(PremiumIndicator ind, Interval interval) {
        return new PremiumCloseIndicator(
                ind.getBase(),
                ind.getBaseExchangeId(),
                ind.getCompareExchangeId(),
                interval,
                ind.getIndicatorType(),
                ind.getPeriod(),
                ind.getValue(),
                service.adjust(ind.getOpenTimestamp(), ind.getCloseTimestamp(),interval)
        );
    }
}
