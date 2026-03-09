package com.example.demo.market_data.infrastructure.persistence.repo;

import com.example.demo.market_data.infrastructure.persistence.entity.TickEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TickRepository extends JpaRepository<TickEntity, Long> {
}
