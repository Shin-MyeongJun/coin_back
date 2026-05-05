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
@Table(name = "economic_indicator")
@Getter
@NoArgsConstructor
public class EcoIndQueryEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "ind_code_id", nullable = false)
    private Long indCodeId;

    @Column(precision = 19, scale = 6)
    private BigDecimal value;

    @Column(name = "observation_date", length = 20)
    private Long observationDate;

    @Column(name = "release_date")
    private Long releaseDate;

    @Column(name = "timestamp", nullable = false)
    private Long timestamp;
}
