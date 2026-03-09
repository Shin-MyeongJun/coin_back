package com.example.demo.market_data.infrastructure.persistence.repo;


import com.example.demo.market_data.infrastructure.persistence.entity.PremiumDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PremiumDetailRepository extends JpaRepository<PremiumDetailEntity, Long> {
    // 필요 시 custom query 메서드 추가 가능
}
