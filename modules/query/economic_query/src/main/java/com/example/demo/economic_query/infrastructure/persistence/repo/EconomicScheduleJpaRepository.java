package com.example.demo.economic_query.infrastructure.persistence.repo;

import com.example.demo.economic_query.infrastructure.persistence.entity.EconomicScheduleQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EconomicScheduleJpaRepository extends JpaRepository<EconomicScheduleQueryEntity, Long> {
    List<EconomicScheduleQueryEntity> findByReleaseDateBetween(Long from, Long to);
}
