package com.example.demo.analytics_query.infrastructure.persistence.repo;

import com.example.demo.analytics_query.infrastructure.persistence.entity.PremiumDetailCandleQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PremiumDetailCandleJpaRepository extends JpaRepository<PremiumDetailCandleQueryEntity, Long> {

    @Query("SELECT e FROM PremiumDetailCandleQueryEntity e WHERE e.symbol = :symbol AND e.baseExchangeId = :baseExchangeId AND e.compareExchangeId = :compareExchangeId AND e.interval = :interval AND e.bucketCloseTs BETWEEN :fromTs AND :toTs ORDER BY e.bucketCloseTs ASC")
    List<PremiumDetailCandleQueryEntity> findSeries(
            @Param("symbol") String symbol, @Param("baseExchangeId") Long baseExchangeId,
            @Param("compareExchangeId") Long compareExchangeId, @Param("interval") String interval,
            @Param("fromTs") long fromTs, @Param("toTs") long toTs);

    @Query("SELECT MAX(e.bucketCloseTs) FROM PremiumDetailCandleQueryEntity e WHERE e.symbol = :symbol AND e.baseExchangeId = :baseExchangeId AND e.compareExchangeId = :compareExchangeId AND e.interval = :interval")
    Optional<Long> findMaxBucketCloseTs(
            @Param("symbol") String symbol, @Param("baseExchangeId") Long baseExchangeId,
            @Param("compareExchangeId") Long compareExchangeId, @Param("interval") String interval);
}
