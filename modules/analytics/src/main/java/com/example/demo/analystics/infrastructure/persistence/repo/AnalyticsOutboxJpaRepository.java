package com.example.demo.analystics.infrastructure.persistence.repo;

import com.example.demo.analystics.infrastructure.persistence.entity.AnalyticsOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnalyticsOutboxJpaRepository extends JpaRepository<AnalyticsOutboxEntity, Long> {

    @Query(value = """
            SELECT *
              FROM analytics_outbox
             WHERE published_at IS NULL
               AND retry_count < :maxRetry
             ORDER BY id ASC
             FOR UPDATE SKIP LOCKED
             LIMIT :batchSize
            """, nativeQuery = true)
    List<AnalyticsOutboxEntity> findPendingForUpdateSkipLocked(
            @Param("maxRetry") int maxRetry,
            @Param("batchSize") int batchSize);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE AnalyticsOutboxEntity a
               SET a.publishedAt = :publishedAt
             WHERE a.id = :id
            """)
    int markPublished(@Param("id") Long id, @Param("publishedAt") Long publishedAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE AnalyticsOutboxEntity a
               SET a.retryCount = a.retryCount + 1
             WHERE a.id = :id
            """)
    int incrementRetry(@Param("id") Long id);
}
