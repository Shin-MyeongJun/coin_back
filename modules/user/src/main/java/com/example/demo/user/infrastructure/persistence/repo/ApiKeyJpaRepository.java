package com.example.demo.user.infrastructure.persistence.repo;

import com.example.demo.user.infrastructure.persistence.entity.ApiKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyJpaRepository extends JpaRepository<ApiKeyEntity, UUID> {
    Optional<ApiKeyEntity> findByPrefix(String prefix);
    List<ApiKeyEntity> findAllByAccountIdOrderByCreatedAtDesc(UUID accountId);
    long countByAccountIdAndRevokedAtIsNull(UUID accountId);
}
