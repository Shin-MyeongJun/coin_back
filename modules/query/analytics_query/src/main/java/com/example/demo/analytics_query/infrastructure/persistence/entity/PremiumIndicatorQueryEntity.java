package com.example.demo.analytics_query.infrastructure.persistence.entity;

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
@Table(name = "premium_indicator")
@Getter
@NoArgsConstructor
public class PremiumIndicatorQueryEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "base_exchange_id", nullable = false)
    private Long baseExchangeId;

    @Column(name = "compare_exchange_id", nullable = false)
    private Long compareExchangeId;

    @Column(name = "interval")
    private String interval;

    @Column(name = "type")
    private String type;

    @Column(name = "period")
    private Integer period;

    @Column(name = "value")
    private BigDecimal value;

    @Column(name = "bucket_open_ts", nullable = false)
    private Long bucketOpenTs;

    @Column(name = "bucket_close_ts", nullable = false)
    private Long bucketCloseTs;

    @Column(name = "observe_open_ts", nullable = false)
    private Long observeOpenTs;

    @Column(name = "observe_close_ts", nullable = false)
    private Long observeCloseTs;
}
