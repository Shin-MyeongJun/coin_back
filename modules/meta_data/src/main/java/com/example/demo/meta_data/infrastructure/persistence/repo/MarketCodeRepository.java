package com.example.demo.meta_data.infrastructure.persistence.repo;


import com.example.demo.meta_data.infrastructure.persistence.entity.MarketCodeEntity;
import com.example.demo.meta_data.infrastructure.persistence.entity.embeddable.MarketCodeKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MarketCodeRepository extends JpaRepository<MarketCodeEntity, Long> {
    Optional<MarketCodeEntity> findByKey(MarketCodeKey key);
}
