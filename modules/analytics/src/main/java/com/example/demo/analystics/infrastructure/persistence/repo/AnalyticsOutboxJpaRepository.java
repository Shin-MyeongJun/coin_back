package com.example.demo.analystics.infrastructure.persistence.repo;

import com.example.demo.analystics.infrastructure.persistence.entity.AnalyticsOutboxEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnalyticsOutboxJpaRepository extends JpaRepository<AnalyticsOutboxEntity, Long> {

    @Query("""
            SELECT a FROM AnalyticsOutboxEntity a
            WHERE a.publishedAt IS NULL
              AND a.retryCount < :maxRetry
            ORDER BY a.id ASC
            """)
    List<AnalyticsOutboxEntity> findPending(@Param("maxRetry") int maxRetry, Pageable pageable);

    @Modifying
    @Query("""
            UPDATE AnalyticsOutboxEntity a
               SET a.publishedAt = :publishedAt
             WHERE a.id = :id
            """)
    int markPublished(@Param("id") Long id, @Param("publishedAt") Long publishedAt);

    @Modifying
    @Query("""
            UPDATE AnalyticsOutboxEntity a
               SET a.retryCount = a.retryCount + 1
             WHERE a.id = :id
            """)
    int incrementRetry(@Param("id") Long id);
}
