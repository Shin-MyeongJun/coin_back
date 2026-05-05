package com.example.demo.economic_query.infrastructure.persistence.repo;

import com.example.demo.economic_query.infrastructure.persistence.entity.EcoIndQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EcoIndJpaRepository extends JpaRepository<EcoIndQueryEntity, Long> {
    List<EcoIndQueryEntity> findByIndCodeIdAndObservationDateBetween(Long indCodeId, Long from, Long to);
}
