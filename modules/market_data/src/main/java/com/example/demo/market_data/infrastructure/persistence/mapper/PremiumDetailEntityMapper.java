package com.example.demo.market_data.infrastructure.persistence.mapper;

import com.example.demo.infra_shard.persistence.EntityMapping;
import com.example.demo.market_data.domain.domain.PremiumDetail;
import com.example.demo.market_data.infrastructure.persistence.entity.PremiumDetailEntity;
import org.springframework.stereotype.Component;

@Component
public class PremiumDetailEntityMapper implements EntityMapping<PremiumDetail, PremiumDetailEntity> {
    @Override
    public PremiumDetailEntity toEntity(PremiumDetail pd) {
        return PremiumDetailEntity.builder()
                .symbol(pd.symbol())
                .baseExchangeId(pd.baseExchangeId())
                .compareExchangeId(pd.compareExchangeId())
                .baseBid(pd.baseBid())
                .baseAsk(pd.baseAsk())
                .baseQuoteVal(pd.baseQuoteVal())
                .compareBid(pd.compareBid())
                .compareAsk(pd.compareAsk())
                .compareQuoteVal(pd.compareQuoteVal())
                .timestamp(pd.timestamp())
                .build();
    }

    @Override
    public PremiumDetail toDomain(PremiumDetailEntity pde) {
        return new PremiumDetail(
                pde.getSymbol(),
                pde.getBaseExchangeId(),
                pde.getCompareExchangeId(),
                pde.getBaseBid(),
                pde.getBaseAsk(),
                pde.getBaseQuoteVal(),
                pde.getCompareBid(),
                pde.getCompareAsk(),
                pde.getCompareQuoteVal(),
                pde.getTimestamp()
        );
    }
}
