package com.example.demo.market_data.infrastructure.persistence.adapter.save;

import com.example.demo.market_data.infrastructure.persistence.adapter.base.PriceValueSaveAdapter;
import com.example.demo.market_data.infrastructure.persistence.entity.TickEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public class TickSaveAdapter extends PriceValueSaveAdapter<TickEntity> {
    public TickSaveAdapter(JpaRepository<TickEntity, Long> repo) {
        super(repo);
    }
}
