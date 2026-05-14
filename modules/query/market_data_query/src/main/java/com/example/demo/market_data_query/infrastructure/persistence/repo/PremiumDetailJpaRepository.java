package com.example.demo.market_data_query.infrastructure.persistence.repo;

import com.example.demo.market_data_query.infrastructure.persistence.entity.PremiumDetailQueryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PremiumDetailJpaRepository extends JpaRepository<PremiumDetailQueryEntity, Long> {

    List<PremiumDetailQueryEntity> findByBaseExchangeIdAndCompareExchangeIdAndSymbolAndTimestampBetween(
            Long baseExchangeId, Long compareExchangeId, String symbol, Long fromTs, Long toTs);

    List<PremiumDetailQueryEntity>
    findByBaseExchangeIdAndCompareExchangeIdAndSymbolAndTimestampLessThanEqualOrderByTimestampDesc(
            Long baseExchangeId, Long compareExchangeId, String symbol, Long timestampCeiling, Pageable pageable);

    List<PremiumDetailQueryEntity>
    findByBaseExchangeIdAndCompareExchangeIdAndSymbolAndTimestampGreaterThanEqualOrderByTimestampAsc(
            Long baseExchangeId, Long compareExchangeId, String symbol, Long timestampFloor, Pageable pageable);
}
