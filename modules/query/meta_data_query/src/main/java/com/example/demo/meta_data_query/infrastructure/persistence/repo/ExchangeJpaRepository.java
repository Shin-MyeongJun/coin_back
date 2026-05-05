package com.example.demo.meta_data_query.infrastructure.persistence.repo;

import com.example.demo.meta_data_query.infrastructure.persistence.entity.ExchangeQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeJpaRepository extends JpaRepository<ExchangeQueryEntity, Long> {
}
