package com.example.demo.analystics.infrastructure.persistence.repo.indicator;

import com.example.demo.analystics.infrastructure.persistence.entity.indicator.TickIndicatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TickIndicatorRepository extends JpaRepository<TickIndicatorEntity, Long> {
}
