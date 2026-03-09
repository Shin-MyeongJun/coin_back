package com.example.demo.analystics.infrastructure.persistence.adapter.save.candle;

import com.example.demo.analystics.infrastructure.persistence.adapter.base.AnalyticsSaveAdapter;
import com.example.demo.analystics.infrastructure.persistence.entity.candle.PremiumDetailCandleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public class PremiumDetailCandleSaveAdapter extends AnalyticsSaveAdapter<PremiumDetailCandleEntity> {
    public PremiumDetailCandleSaveAdapter(JpaRepository<PremiumDetailCandleEntity, Long> repo) {
        super(repo);
    }
}
