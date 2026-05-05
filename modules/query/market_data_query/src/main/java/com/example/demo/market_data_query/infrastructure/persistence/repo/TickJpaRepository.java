package com.example.demo.market_data_query.infrastructure.persistence.repo;

import com.example.demo.market_data_query.infrastructure.persistence.entity.TickQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TickJpaRepository extends JpaRepository<TickQueryEntity, Long> {

    Optional<TickQueryEntity> findTop1ByMarketCodeIdOrderByTimestampDesc(Long marketCodeId);
}
