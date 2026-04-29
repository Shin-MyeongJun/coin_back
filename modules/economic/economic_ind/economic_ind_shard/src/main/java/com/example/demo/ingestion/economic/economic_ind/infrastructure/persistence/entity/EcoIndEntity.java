package com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(
        name = "economic_indicator"
)
@SequenceGenerator(
        name = "economic_indicator_seq_gen",
        sequenceName = "economic_indicator_seq",
        allocationSize = 100   // ← 하이버네이트가 100개씩 ID 블록을 미리 가져와 round-trip을 줄임
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EcoIndEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "economic_indicator_seq_gen")
    private Long id;

    // 네이밍 수정: 카멜 케이스 적용
    @Column(name = "ind_code_id", nullable = false)
    private Long indCodeId;

    // 소수점 유실 방지를 위한 제약 조건 추가 (총 19자리, 소수점 6자리)
    @Column(precision = 19, scale = 6)
    private BigDecimal value;

    @Column(name = "observation_date", length = 20)
    private Long observationDate; // 데이터 시점 (예: "2026-03")

    // DB에서 사람이 읽을 수 있도록 Instant(UTC) 사용 권장 (DTO에서 파싱 필요)
    @Column(name = "release_date")
    private Long releaseDate; // 발표일시

    @Column(name = "timestamp", nullable = false)
    private Long timestamp;  // 시스템 저장/수집 시각
}
