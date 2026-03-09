package com.example.demo.market_data.infrastructure.persistence.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Entity
@Table(
        name = "premium"
)
@SequenceGenerator(
        name = "premium_seq_gen",
        sequenceName = "premium_seq",
        allocationSize = 1000   // ← 하이버네이트가 1000개씩 ID 블록을 미리 가져와 round-trip을 줄임
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PremiumEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE ,generator ="premium_seq_gen" )
    private Long id;

    @Column(nullable = false)
    private String symbol;

    @Column(name = "base_exchange_id", nullable = false)
    private Long baseExchangeId;

    @Column(name = "compare_exchange_id", nullable = false)
    private Long compareExchangeId;

    @Column(nullable = false)
    private Long timestamp;

    @Column(nullable = false, precision = 30, scale = 10)
    private BigDecimal bid;

    @Column(nullable = false, precision = 30, scale = 10)
    private BigDecimal ask;

    // getter/setter 생략
}
