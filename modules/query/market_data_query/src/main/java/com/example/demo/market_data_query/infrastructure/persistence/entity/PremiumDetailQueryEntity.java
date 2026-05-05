package com.example.demo.market_data_query.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Immutable
@Entity
@Table(name = "premium_detail")
@Getter
@NoArgsConstructor
public class PremiumDetailQueryEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column
    private String symbol;

    @Column(name = "base_exchange_id", nullable = false)
    private Long baseExchangeId;

    @Column(name = "compare_exchange_id", nullable = false)
    private Long compareExchangeId;

    @Column(name = "base_bid", nullable = false, precision = 30, scale = 10)
    private BigDecimal baseBid;

    @Column(name = "base_ask", nullable = false, precision = 30, scale = 10)
    private BigDecimal baseAsk;

    @Column(name = "base_quote_val", nullable = false, precision = 30, scale = 10)
    private BigDecimal baseQuoteVal;

    @Column(name = "compare_bid", nullable = false, precision = 30, scale = 10)
    private BigDecimal compareBid;

    @Column(name = "compare_ask", nullable = false, precision = 30, scale = 10)
    private BigDecimal compareAsk;

    @Column(name = "compare_quote_val", nullable = false, precision = 30, scale = 10)
    private BigDecimal compareQuoteVal;

    @Column(nullable = false)
    private Long timestamp;
}
