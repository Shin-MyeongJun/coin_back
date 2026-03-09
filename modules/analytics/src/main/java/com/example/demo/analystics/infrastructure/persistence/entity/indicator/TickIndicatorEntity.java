package com.example.demo.analystics.infrastructure.persistence.entity.indicator;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.indicator.TradeIndicatorType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(
        name = "tick_indicator"
)
@SequenceGenerator(
        name = "tick_indicator_seq_gen",
        sequenceName = "tick_indicator_seq",
        allocationSize = 1000   // ← 하이버네이트가 1000개씩 ID 블록을 미리 가져와 round-trip을 줄임
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TickIndicatorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "tick_indicator_seq_gen")
    private Long id;

    @Column(name ="market_code_id")
    private Long marketCodeId;

    @Column
    @Enumerated(EnumType.STRING)
    private Interval interval;

    @Column
    @Enumerated(EnumType.STRING)
    private TradeIndicatorType type;

    @Column
    private int period;

    @Column
    private BigDecimal value;

    @Column(nullable = false)
    private Long bucketOpenTs;

    @Column(nullable = false)
    private Long bucketCloseTs;

    @Column(nullable = false)
    private Long observeOpenTs;

    @Column(nullable = false)
    private Long observeCloseTs;


}
