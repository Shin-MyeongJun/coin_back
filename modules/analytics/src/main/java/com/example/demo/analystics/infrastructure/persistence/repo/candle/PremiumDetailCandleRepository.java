package com.example.demo.analystics.infrastructure.persistence.repo.candle;

import com.example.demo.analystics.infrastructure.persistence.entity.candle.PremiumDetailCandleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PremiumDetailCandleRepository  extends JpaRepository<PremiumDetailCandleEntity, Long> {
}
