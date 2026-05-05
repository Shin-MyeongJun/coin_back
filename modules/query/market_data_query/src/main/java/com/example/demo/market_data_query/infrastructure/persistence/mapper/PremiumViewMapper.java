package com.example.demo.market_data_query.infrastructure.persistence.mapper;

import com.example.demo.market_data_query.application.dto.PremiumDetailView;
import com.example.demo.market_data_query.infrastructure.persistence.entity.PremiumDetailQueryEntity;
import org.springframework.stereotype.Component;

@Component
public class PremiumViewMapper {

    public PremiumDetailView toDetailView(PremiumDetailQueryEntity e) {
        return new PremiumDetailView(
                e.getId(), e.getSymbol(),
                e.getBaseExchangeId(), e.getCompareExchangeId(),
                e.getBaseBid(), e.getBaseAsk(), e.getBaseQuoteVal(),
                e.getCompareBid(), e.getCompareAsk(), e.getCompareQuoteVal(),
                e.getTimestamp()
        );
    }
}
