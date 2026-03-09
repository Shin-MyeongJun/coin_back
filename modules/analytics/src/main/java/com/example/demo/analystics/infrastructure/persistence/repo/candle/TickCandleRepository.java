package com.example.demo.analystics.infrastructure.persistence.repo.candle;

import com.example.demo.analystics.infrastructure.persistence.entity.candle.TickCandleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TickCandleRepository  extends JpaRepository<TickCandleEntity, Long> {
}
