package com.example.demo.analytics_query.infrastructure.persistence.repo;

import com.example.demo.analytics_query.infrastructure.persistence.entity.PremiumIndicatorQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PremiumIndicatorJpaRepository extends JpaRepository<PremiumIndicatorQueryEntity, Long> {

    @Query("SELECT e FROM PremiumIndicatorQueryEntity e WHERE e.symbol = :symbol AND e.baseExchangeId = :baseExchangeId AND e.compareExchangeId = :compareExchangeId AND e.interval = :interval AND e.type = :type AND e.bucketOpenTs BETWEEN :from AND :to")
    List<PremiumIndicatorQueryEntity> findSeries(
            @Param("symbol") String symbol, @Param("baseExchangeId") Long baseExchangeId,
            @Param("compareExchangeId") Long compareExchangeId, @Param("interval") String interval,
            @Param("type") String type, @Param("from") Long from, @Param("to") Long to);

    @Query("SELECT e FROM PremiumIndicatorQueryEntity e WHERE e.symbol = :symbol AND e.baseExchangeId = :baseExchangeId AND e.compareExchangeId = :compareExchangeId AND e.interval = :interval AND e.type = :type ORDER BY e.bucketCloseTs DESC, e.id DESC")
    List<PremiumIndicatorQueryEntity> findLatest(
            @Param("symbol") String symbol, @Param("baseExchangeId") Long baseExchangeId,
            @Param("compareExchangeId") Long compareExchangeId, @Param("interval") String interval,
            @Param("type") String type, org.springframework.data.domain.Pageable pageable);
}
