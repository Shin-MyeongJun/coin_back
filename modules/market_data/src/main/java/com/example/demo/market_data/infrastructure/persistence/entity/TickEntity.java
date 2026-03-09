package com.example.demo.market_data.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Entity
@Table(
        name = "tick"
)
@SequenceGenerator(
        name = "tick_seq_gen",
        sequenceName = "tick_seq",
        allocationSize = 1000   // ← 하이버네이트가 1000개씩 ID 블록을 미리 가져와 round-trip을 줄임
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TickEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE ,generator = "tick_seq_gen")
    private Long id;

    @Column(name = "market_code_id", nullable = false)
    private Long marketCodeId;

    @Column(nullable = false)
    private Long timestamp;

    @Column(nullable = false,precision = 30, scale = 10)
    private BigDecimal bid;

    @Column(nullable = false,precision = 30, scale = 10)
    private BigDecimal ask;

    // getter/setter 생략
}