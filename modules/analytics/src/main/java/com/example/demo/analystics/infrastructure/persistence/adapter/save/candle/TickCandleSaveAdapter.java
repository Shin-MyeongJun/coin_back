package com.example.demo.analystics.infrastructure.persistence.adapter.save.candle;

import com.example.demo.analystics.infrastructure.persistence.adapter.base.AnalyticsSaveAdapter;
import com.example.demo.analystics.infrastructure.persistence.entity.candle.TickCandleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public class TickCandleSaveAdapter extends AnalyticsSaveAdapter<TickCandleEntity> {
    public TickCandleSaveAdapter(JpaRepository<TickCandleEntity, Long> repo) {
        super(repo);
    }
}
