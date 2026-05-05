package com.example.demo.meta_data_query.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Immutable
@Entity
@Table(name = "exchange")
@Getter
@NoArgsConstructor
public class ExchangeQueryEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "exchange_type", nullable = false, length = 32)
    private String exchangeType;

    @Column(name = "quote", nullable = false, length = 32)
    private String quote;

    @Column(name = "country", length = 64)
    private String country;

    @Column(name = "status", nullable = false, length = 32)
    private String status;
}
