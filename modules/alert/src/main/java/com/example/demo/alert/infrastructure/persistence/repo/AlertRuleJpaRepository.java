package com.example.demo.alert.infrastructure.persistence.repo;

import com.example.demo.alert.infrastructure.persistence.entity.AlertRuleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertRuleJpaRepository extends JpaRepository<AlertRuleEntity, Long> {
    Optional<AlertRuleEntity> findByIdAndUserId(Long id, String userId);

    Page<AlertRuleEntity> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    List<AlertRuleEntity> findByActiveTrue();

    List<AlertRuleEntity> findByTargetTypeAndAssetSymbolAndActiveTrue(String targetType, String assetSymbol);
}
