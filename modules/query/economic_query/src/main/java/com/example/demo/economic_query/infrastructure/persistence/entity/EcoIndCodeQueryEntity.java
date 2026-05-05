package com.example.demo.economic_query.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Immutable
@Entity
@Table(name = "economic_indicator_code")
@Getter
@NoArgsConstructor
public class EcoIndCodeQueryEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "indicator_code", unique = true)
    private String indicatorCode;

    @Column(name = "country")
    private String country;

    @Column(name = "type")
    private String type;

    @Column(name = "frequency")
    private String frequency;

    @Column(name = "unit")
    private String unit;
}
