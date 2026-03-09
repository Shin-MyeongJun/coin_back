package com.example.demo.analystics.infrastructure.persistence.repo.candle;

import com.example.demo.analystics.infrastructure.persistence.entity.candle.PremiumCandleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PremiumCandleRepository  extends JpaRepository<PremiumCandleEntity, Long> {
}
