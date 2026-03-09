package com.example.demo.market_data.infrastructure.persistence.adapter.save;

import com.example.demo.market_data.infrastructure.persistence.adapter.base.PriceValueSaveAdapter;
import com.example.demo.market_data.infrastructure.persistence.entity.PremiumEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public class PremiumSaveAdapter extends PriceValueSaveAdapter<PremiumEntity> {
    public PremiumSaveAdapter(JpaRepository<PremiumEntity, Long> repo) {
        super(repo);
    }
}
