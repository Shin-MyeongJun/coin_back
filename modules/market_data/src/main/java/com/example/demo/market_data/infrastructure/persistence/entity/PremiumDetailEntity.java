package com.example.demo.market_data.infrastructure.persistence.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(
        name = "premium_detail"
)
@SequenceGenerator(
        name = "premium_detail_seq_gen",
        sequenceName = "premium_detail_seq",
        allocationSize = 1000   // ← 하이버네이트가 1000개씩 ID 블록을 미리 가져와 round-trip을 줄임
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PremiumDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "premium_detail_seq_gen")
    private Long id;

    @Column
    private String symbol;

    @Column(name = "base_exchange_id", nullable = false)
    private Long baseExchangeId;

    @Column(name = "compare_exchange_id", nullable = false)
    private Long compareExchangeId;

    @Column(name="base_bid", nullable = false, precision = 30, scale = 10)
    private BigDecimal baseBid;

    @Column(name="base_ask",nullable = false, precision = 30, scale = 10)
    private BigDecimal baseAsk;

    @Column(name="base_quote_val",nullable = false, precision = 30, scale = 10)
    private BigDecimal baseQuoteVal;

    @Column(name="compare_bid",nullable = false, precision = 30, scale = 10)
    private BigDecimal compareBid;

    @Column(name="compare_ask",nullable = false, precision = 30, scale = 10)
    private BigDecimal compareAsk;

    @Column(name="compare_quote_val",nullable = false, precision = 30, scale = 10)
    private BigDecimal compareQuoteVal;

    @Column(nullable = false)
    private Long timestamp;

}
