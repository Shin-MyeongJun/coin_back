package com.example.demo.analystics.infrastructure.persistence.repo.indicator;

import com.example.demo.analystics.infrastructure.persistence.entity.indicator.PremiumIndicatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PremiumIndicatorRepository  extends JpaRepository<PremiumIndicatorEntity, Long> {
}
