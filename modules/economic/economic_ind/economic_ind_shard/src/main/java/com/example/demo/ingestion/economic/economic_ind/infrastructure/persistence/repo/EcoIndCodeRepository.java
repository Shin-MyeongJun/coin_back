package com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.repo;

import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EcoIndCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface EcoIndCodeRepository extends JpaRepository<EcoIndCodeEntity, Long> {
    // 필요 시 custom query 메서드 추가 가능
}
