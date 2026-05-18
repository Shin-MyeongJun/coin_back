package com.example.demo.alert.infrastructure.persistence.repo;

import com.example.demo.alert.infrastructure.persistence.entity.AlertFiringEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertFiringJpaRepository extends JpaRepository<AlertFiringEntity, Long> {
    List<AlertFiringEntity> findByUserIdOrderByFiredAtDesc(String userId, Pageable pageable);

    List<AlertFiringEntity> findByUserIdAndFiredAtLessThanOrderByFiredAtDesc(
            String userId,
            long firedAt,
            Pageable pageable
    );
}
