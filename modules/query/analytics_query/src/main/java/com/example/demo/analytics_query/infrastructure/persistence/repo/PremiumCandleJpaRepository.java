package com.example.demo.analytics_query.infrastructure.persistence.repo;

import com.example.demo.analytics_query.infrastructure.persistence.entity.PremiumCandleQueryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PremiumCandleJpaRepository extends JpaRepository<PremiumCandleQueryEntity, Long> {

    @Query("SELECT e FROM PremiumCandleQueryEntity e WHERE e.symbol = :symbol AND e.baseExchangeId = :baseExchangeId AND e.compareExchangeId = :compareExchangeId AND e.interval = :interval AND e.bucketOpenTs BETWEEN :from AND :to")
    List<PremiumCandleQueryEntity> findSeries(
            @Param("symbol") String symbol, @Param("baseExchangeId") Long baseExchangeId,
            @Param("compareExchangeId") Long compareExchangeId, @Param("interval") String interval,
            @Param("from") Long from, @Param("to") Long to);

    @Query("SELECT e FROM PremiumCandleQueryEntity e WHERE e.symbol = :symbol AND e.baseExchangeId = :baseExchangeId AND e.compareExchangeId = :compareExchangeId AND e.interval = :interval ORDER BY e.bucketOpenTs DESC")
    List<PremiumCandleQueryEntity> findMiniChart(
            @Param("symbol") String symbol, @Param("baseExchangeId") Long baseExchangeId,
            @Param("compareExchangeId") Long compareExchangeId, @Param("interval") String interval,
            Pageable pageable);

    @Query("SELECT MAX(e.bucketCloseTs) FROM PremiumCandleQueryEntity e WHERE e.symbol = :symbol AND e.baseExchangeId = :baseExchangeId AND e.compareExchangeId = :compareExchangeId AND e.interval = :interval")
    Optional<Long> findMaxBucketCloseTs(
            @Param("symbol") String symbol, @Param("baseExchangeId") Long baseExchangeId,
            @Param("compareExchangeId") Long compareExchangeId, @Param("interval") String interval);
}
