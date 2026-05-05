package com.example.demo.economic_query.infrastructure.persistence.entity;

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
@Table(name = "asset_indicator_correlation")
@Getter
@NoArgsConstructor
public class CorrelationResultQueryEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "asset_symbol", nullable = false, length = 32)
    private String assetSymbol;

    @Column(name = "indicator_code", nullable = false, length = 128)
    private String indicatorCode;

    @Column(name = "correlation", precision = 10, scale = 6)
    private BigDecimal correlation;

    @Column(name = "period_days")
    private Integer periodDays;

    @Column(name = "calculated_at")
    private Long calculatedAt;
}
