package com.example.demo.analytics_query.infrastructure.persistence.repo;

import com.example.demo.analytics_query.infrastructure.persistence.entity.TickCandleQueryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TickCandleJpaRepository extends JpaRepository<TickCandleQueryEntity, Long> {

    List<TickCandleQueryEntity> findByMarketCodeIdAndIntervalAndBucketOpenTsBetween(
            Long marketCodeId, String interval, Long from, Long to);

    List<TickCandleQueryEntity> findByMarketCodeIdAndIntervalOrderByBucketOpenTsDesc(
            Long marketCodeId, String interval, Pageable pageable);

    @Query("SELECT MAX(e.bucketCloseTs) FROM TickCandleQueryEntity e WHERE e.marketCodeId = :marketCodeId AND e.interval = :interval")
    Optional<Long> findMaxBucketCloseTs(@Param("marketCodeId") Long marketCodeId, @Param("interval") String interval);
}
