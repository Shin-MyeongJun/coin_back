package com.example.demo.analystics.infrastructure.persistence.adapter.save.indicator;

import com.example.demo.analystics.infrastructure.persistence.adapter.base.AnalyticsSaveAdapter;
import com.example.demo.analystics.infrastructure.persistence.entity.indicator.TickIndicatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public class TickIndicatorSaveAdapter extends AnalyticsSaveAdapter<TickIndicatorEntity> {

    public TickIndicatorSaveAdapter(JpaRepository<TickIndicatorEntity, Long> repo) {
        super(repo);
    }
}
