package com.example.demo.analytics_query.infrastructure.persistence.repo;

import com.example.demo.analytics_query.infrastructure.persistence.entity.TickIndicatorQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TickIndicatorJpaRepository extends JpaRepository<TickIndicatorQueryEntity, Long> {

    List<TickIndicatorQueryEntity> findByMarketCodeIdAndIntervalAndTypeAndBucketOpenTsBetween(
            Long marketCodeId, String interval, String type, Long from, Long to);

    Optional<TickIndicatorQueryEntity> findTop1ByMarketCodeIdAndIntervalAndTypeOrderByBucketCloseTsDescIdDesc(
            Long marketCodeId, String interval, String type);
}
