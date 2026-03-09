package com.example.demo.analystics.infrastructure.persistence.adapter.save.indicator;

import com.example.demo.analystics.infrastructure.persistence.adapter.base.AnalyticsSaveAdapter;
import com.example.demo.analystics.infrastructure.persistence.entity.indicator.PremiumIndicatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public class PremiumIndicatorSaveAdapter extends AnalyticsSaveAdapter<PremiumIndicatorEntity> {

    public PremiumIndicatorSaveAdapter(JpaRepository<PremiumIndicatorEntity, Long> repo) {
        super(repo);
    }
}
