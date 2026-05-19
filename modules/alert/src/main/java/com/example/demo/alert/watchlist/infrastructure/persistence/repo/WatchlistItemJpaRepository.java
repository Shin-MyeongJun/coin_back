package com.example.demo.alert.watchlist.infrastructure.persistence.repo;

import com.example.demo.alert.watchlist.infrastructure.persistence.entity.WatchlistItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WatchlistItemJpaRepository extends JpaRepository<WatchlistItemEntity, Long> {
    Optional<WatchlistItemEntity> findByIdAndUserId(Long id, String userId);

    Page<WatchlistItemEntity> findByUserId(String userId, Pageable pageable);

    Page<WatchlistItemEntity> findByUserIdAndSymbolContainingIgnoreCase(String userId, String keyword, Pageable pageable);
}
