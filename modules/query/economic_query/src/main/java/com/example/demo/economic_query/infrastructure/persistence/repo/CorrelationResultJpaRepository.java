package com.example.demo.economic_query.infrastructure.persistence.repo;

import com.example.demo.economic_query.infrastructure.persistence.entity.CorrelationResultQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CorrelationResultJpaRepository extends JpaRepository<CorrelationResultQueryEntity, Long> {
    List<CorrelationResultQueryEntity> findByAssetSymbol(String assetSymbol);
}
