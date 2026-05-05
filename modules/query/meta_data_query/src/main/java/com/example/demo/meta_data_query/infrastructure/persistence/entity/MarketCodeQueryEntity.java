package com.example.demo.meta_data_query.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Immutable
@Entity
@Table(name = "market_code")
@Getter
@NoArgsConstructor
public class MarketCodeQueryEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "exchange_id", nullable = false)
    private Long exchangeId;

    @Column(name = "base", nullable = false, length = 32)
    private String base;

    @Column(name = "quote", nullable = false, length = 32)
    private String quote;

    @Column(name = "trading_pair", length = 64)
    private String tradingPair;
}
