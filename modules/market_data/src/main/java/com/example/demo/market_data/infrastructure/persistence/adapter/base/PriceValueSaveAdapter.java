package com.example.demo.market_data.infrastructure.persistence.adapter.base;

import com.example.demo.market_data.application.port.out.SaveAndFlushPriseValuePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@RequiredArgsConstructor
public abstract class PriceValueSaveAdapter<ENTITY> implements SaveAndFlushPriseValuePort<ENTITY> {

    private final JpaRepository<ENTITY,Long> repo;

    @Override
    public void saveAll(List<ENTITY> entity) {
            repo.saveAll(entity);
    }

    @Override
    public void flush() {
         repo.flush();
    }
}
