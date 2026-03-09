package com.example.demo.analystics.infrastructure.persistence.adapter.save.candle;

import com.example.demo.analystics.infrastructure.persistence.adapter.base.AnalyticsSaveAdapter;
import com.example.demo.analystics.infrastructure.persistence.entity.candle.PremiumCandleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public class PremiumCandleSaveAdapter extends AnalyticsSaveAdapter<PremiumCandleEntity> {
    public PremiumCandleSaveAdapter(JpaRepository<PremiumCandleEntity, Long> repo) {
        super(repo);
    }
}
