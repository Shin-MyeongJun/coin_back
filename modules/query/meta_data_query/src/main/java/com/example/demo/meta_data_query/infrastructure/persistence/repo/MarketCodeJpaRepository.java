package com.example.demo.meta_data_query.infrastructure.persistence.repo;

import com.example.demo.meta_data_query.infrastructure.persistence.entity.MarketCodeQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarketCodeJpaRepository extends JpaRepository<MarketCodeQueryEntity, Long> {
    List<MarketCodeQueryEntity> findByExchangeId(Long exchangeId);
}
