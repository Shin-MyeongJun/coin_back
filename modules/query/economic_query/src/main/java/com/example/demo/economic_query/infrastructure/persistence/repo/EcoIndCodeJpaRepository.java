package com.example.demo.economic_query.infrastructure.persistence.repo;

import com.example.demo.economic_query.infrastructure.persistence.entity.EcoIndCodeQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EcoIndCodeJpaRepository extends JpaRepository<EcoIndCodeQueryEntity, Long> {
    List<EcoIndCodeQueryEntity> findByType(String type);
}
