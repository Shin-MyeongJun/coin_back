package com.example.demo.meta_data.infrastructure.persistence.adapter;

import com.example.demo.meta_data.application.port.out.FindAndSavePort;
import com.example.demo.meta_data.infrastructure.persistence.entity.ExchangeEntity;
import com.example.demo.meta_data.infrastructure.persistence.entity.embeddable.ExchangeKey;
import com.example.demo.meta_data.infrastructure.persistence.repo.ExchangeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExchangeRepositoryAdapter implements FindAndSavePort<ExchangeEntity, ExchangeKey> {

    private final ExchangeRepository repo;

    @Override
    public Optional<ExchangeEntity> findByKey(ExchangeKey exchangeKey) {
        return repo.findByKey(exchangeKey);
    }

    @Override
    public ExchangeEntity save(ExchangeEntity exchangeEntity) {
        return repo.save(exchangeEntity);
    }
}
