package com.example.demo.market_data.infrastructure.persistence.adapter.writer;

import com.example.demo.infra_shard.persistence.DomainToEntity;
import com.example.demo.market_data.domain.domain.PremiumDetail;
import com.example.demo.market_data.infrastructure.persistence.adapter.base.PriceValueSaveAdapter;
import com.example.demo.market_data.infrastructure.persistence.adapter.base.WritePriceValueAdapter;
import com.example.demo.market_data.infrastructure.persistence.entity.PremiumDetailEntity;
import org.springframework.stereotype.Component;

@Component
public final class WritePremiumDetailAdapter extends WritePriceValueAdapter<PremiumDetail, PremiumDetailEntity> {
    public WritePremiumDetailAdapter(PriceValueSaveAdapter<PremiumDetailEntity> adapter,
                               DomainToEntity<PremiumDetail,PremiumDetailEntity> mapper) {
        super(adapter, mapper);
    }
}