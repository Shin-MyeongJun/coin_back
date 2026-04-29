package com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.repo;

import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EconomicScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EconomicScheduleRepository extends JpaRepository<EconomicScheduleEntity, Long> {
    // 필요 시 custom query 메서드 추가 가능
    @Query("SELECT e FROM EconomicScheduleEntity e WHERE e.status = 'PENDING'")
    List<EconomicScheduleEntity> findAllPendingSchedules();
}
