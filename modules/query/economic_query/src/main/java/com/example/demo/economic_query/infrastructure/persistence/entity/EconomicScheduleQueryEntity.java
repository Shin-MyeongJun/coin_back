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
@Table(name = "economic_schedule")
@Getter
@NoArgsConstructor
public class EconomicScheduleQueryEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "ind_code_id", nullable = false)
    private Long indCodeId;

    @Column(name = "release_code", unique = true)
    private String releaseCode;

    @Column(name = "release_date", nullable = false, length = 10)
    private Long releaseDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "fetched_at")
    private Long fetchedAt;
}
