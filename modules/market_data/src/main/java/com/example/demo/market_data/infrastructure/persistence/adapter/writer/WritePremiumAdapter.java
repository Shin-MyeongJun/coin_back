package com.example.demo.market_data.infrastructure.persistence.adapter.writer;


import com.example.demo.infra_shard.persistence.DomainToEntity;
import com.example.demo.market_data.domain.domain.Premium;
import com.example.demo.market_data.infrastructure.persistence.adapter.base.PriceValueSaveAdapter;
import com.example.demo.market_data.infrastructure.persistence.adapter.base.WritePriceValueAdapter;
import com.example.demo.market_data.infrastructure.persistence.entity.PremiumEntity;
import org.springframework.stereotype.Component;

@Component
public final class WritePremiumAdapter extends WritePriceValueAdapter<Premium, PremiumEntity> {
    public WritePremiumAdapter(PriceValueSaveAdapter<PremiumEntity> adapter, DomainToEntity<Premium,PremiumEntity> mapper) {
        super(adapter, mapper);
    }
}
