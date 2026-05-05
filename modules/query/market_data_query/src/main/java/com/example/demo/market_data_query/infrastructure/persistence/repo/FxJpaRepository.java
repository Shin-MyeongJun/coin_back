package com.example.demo.market_data_query.infrastructure.persistence.repo;

import com.example.demo.market_data_query.infrastructure.persistence.entity.FxQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FxJpaRepository extends JpaRepository<FxQueryEntity, Long> {

    Optional<FxQueryEntity> findTop1ByBaseCurrencyAndQuoteCurrencyOrderByTimestampDesc(
            String baseCurrency, String quoteCurrency);

    List<FxQueryEntity> findByBaseCurrencyAndQuoteCurrencyAndTimestampBetween(
            String baseCurrency, String quoteCurrency, Long fromTs, Long toTs);
}
