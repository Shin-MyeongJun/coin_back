package com.example.demo.meta_data.infrastructure.persistence.adapter;

import com.example.demo.meta_data.application.port.out.FindAndSavePort;
import com.example.demo.meta_data.infrastructure.persistence.entity.MarketCodeEntity;
import com.example.demo.meta_data.infrastructure.persistence.entity.embeddable.MarketCodeKey;
import com.example.demo.meta_data.infrastructure.persistence.repo.MarketCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MarketCodeRepositoryAdapter implements FindAndSavePort<MarketCodeEntity, MarketCodeKey> {

    private final MarketCodeRepository repo;

    @Override
    public Optional<MarketCodeEntity> findByKey(MarketCodeKey marketCodeKey) {
        return repo.findByKey(marketCodeKey);
    }
    @Override
    public MarketCodeEntity save(MarketCodeEntity marketCodeEntity) {
        return repo.save(marketCodeEntity);
    }
}
