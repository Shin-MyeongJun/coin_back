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
@Table(name = "tick")
@Getter
@NoArgsConstructor
public class TickQueryEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "market_code_id", nullable = false)
    private Long marketCodeId;

    @Column(nullable = false)
    private Long timestamp;

    @Column(nullable = false, precision = 30, scale = 10)
    private BigDecimal bid;

    @Column(nullable = false, precision = 30, scale = 10)
    private BigDecimal ask;
}
