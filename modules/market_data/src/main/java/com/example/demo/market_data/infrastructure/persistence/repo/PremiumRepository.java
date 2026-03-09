package com.example.demo.market_data.infrastructure.persistence.repo;


import com.example.demo.market_data.infrastructure.persistence.entity.PremiumEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PremiumRepository extends JpaRepository<PremiumEntity, Long> {
}
