package com.example.demo.analytics_query.infrastructure.persistence.mapper;

import com.example.demo.analytics_query.application.dto.PremiumDetailCandleView;
import com.example.demo.analytics_query.application.dto.PremiumDetailCandleView.PremiumDetailOHLC;
import com.example.demo.analytics_query.infrastructure.persistence.entity.PremiumDetailCandleQueryEntity;
import org.springframework.stereotype.Component;

@Component
public class PremiumDetailCandleViewMapper {

    public PremiumDetailCandleView toView(PremiumDetailCandleQueryEntity e) {
        return new PremiumDetailCandleView(
                e.getSymbol(), e.getBaseExchangeId(), e.getCompareExchangeId(),
                e.getInterval(),
                new PremiumDetailOHLC(e.getOpenBasePrice(), e.getOpenBaseQuoteVal(), e.getOpenComparePrice(), e.getOpenCompareQuoteVal()),
                new PremiumDetailOHLC(e.getHighBasePrice(), e.getHighBaseQuoteVal(), e.getHighComparePrice(), e.getHighCompareQuoteVal()),
                new PremiumDetailOHLC(e.getLowBasePrice(), e.getLowBaseQuoteVal(), e.getLowComparePrice(), e.getLowCompareQuoteVal()),
                new PremiumDetailOHLC(e.getCloseBasePrice(), e.getCloseBaseQuoteVal(), e.getCloseComparePrice(), e.getCloseCompareQuoteVal()),
                e.getBucketOpenTs(), e.getBucketCloseTs(),
                e.getObserveOpenTs(), e.getObserveCloseTs()
        );
    }
}
