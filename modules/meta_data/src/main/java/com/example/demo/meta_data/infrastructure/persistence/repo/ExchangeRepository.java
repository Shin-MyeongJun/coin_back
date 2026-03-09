package com.example.demo.meta_data.infrastructure.persistence.repo;


import com.example.demo.meta_data.infrastructure.persistence.entity.ExchangeEntity;
import com.example.demo.meta_data.infrastructure.persistence.entity.embeddable.ExchangeKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExchangeRepository extends JpaRepository<ExchangeEntity, Long> {
    Optional<ExchangeEntity> findByKey(ExchangeKey key);
}
