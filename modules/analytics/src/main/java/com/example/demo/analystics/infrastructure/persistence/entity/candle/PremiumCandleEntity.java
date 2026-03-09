package com.example.demo.analystics.infrastructure.persistence.entity.candle;


import com.example.demo.analystics.domain.domain.Interval;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(
        name = "premium_candle"
)
@SequenceGenerator(
        name = "premium_candle_seq_gen",
        sequenceName = "premium_candle_seq",
        allocationSize = 1000   // ← 하이버네이트가 1000개씩 ID 블록을 미리 가져와 round-trip을 줄임
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PremiumCandleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "premium_candle_seq_gen")
    private Long id;
    @Column
    private String symbol;

    @Column(name = "base_exchange_id", nullable = false)
    private Long baseExchangeId;

    @Column(name = "compare_exchange_id", nullable = false)
    private Long compareExchangeId;

    @Column
    @Enumerated(EnumType.STRING)
    private Interval interval;


    @Column
    private BigDecimal open;

    @Column
    private BigDecimal high;

    @Column
    private BigDecimal low;

    @Column
    private BigDecimal close;

    @Column(nullable = false)
    private Long bucketOpenTs;

    @Column(nullable = false)
    private Long bucketCloseTs;

    @Column(nullable = false)
    private Long observeOpenTs;

    @Column(nullable = false)
    private Long observeCloseTs;


}
