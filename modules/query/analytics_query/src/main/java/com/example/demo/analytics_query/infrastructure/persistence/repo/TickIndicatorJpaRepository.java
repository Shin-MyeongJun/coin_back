package com.example.demo.analytics_query.infrastructure.persistence.repo;

import com.example.demo.analytics_query.infrastructure.persistence.entity.TickIndicatorQueryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TickIndicatorJpaRepository extends JpaRepository<TickIndicatorQueryEntity, Long> {

    List<TickIndicatorQueryEntity> findByMarketCodeIdAndIntervalAndTypeAndBucketOpenTsBetween(
            Long marketCodeId, String interval, String type, Long from, Long to);

    Optional<TickIndicatorQueryEntity> findTop1ByMarketCodeIdAndIntervalAndTypeOrderByBucketCloseTsDescIdDesc(
            Long marketCodeId, String interval, String type);

    @Query("""
            SELECT e FROM TickIndicatorQueryEntity e
            WHERE e.marketCodeId = :marketCodeId
              AND e.interval     = :interval
              AND e.type         = :type
              AND (:cursor IS NULL OR e.bucketOpenTs <= :cursor)
            ORDER BY e.bucketOpenTs DESC
            """)
    List<TickIndicatorQueryEntity> findCursorBackward(
            @Param("marketCodeId") Long marketCodeId, @Param("interval") String interval,
            @Param("type") String type, @Param("cursor") Long cursor, Pageable pageable);

    @Query("""
            SELECT e FROM TickIndicatorQueryEntity e
            WHERE e.marketCodeId = :marketCodeId
              AND e.interval     = :interval
              AND e.type         = :type
              AND (:cursor IS NULL OR e.bucketOpenTs >= :cursor)
            ORDER BY e.bucketOpenTs ASC
            """)
    List<TickIndicatorQueryEntity> findCursorForward(
            @Param("marketCodeId") Long marketCodeId, @Param("interval") String interval,
            @Param("type") String type, @Param("cursor") Long cursor, Pageable pageable);
}
