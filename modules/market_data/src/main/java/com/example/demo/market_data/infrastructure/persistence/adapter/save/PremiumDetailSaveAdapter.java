package com.example.demo.market_data.infrastructure.persistence.adapter.save;

import com.example.demo.market_data.infrastructure.persistence.adapter.base.PriceValueSaveAdapter;
import com.example.demo.market_data.infrastructure.persistence.entity.PremiumDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public class PremiumDetailSaveAdapter extends PriceValueSaveAdapter<PremiumDetailEntity> {
    public PremiumDetailSaveAdapter(JpaRepository<PremiumDetailEntity, Long> repo) {
        super(repo);
    }
}
