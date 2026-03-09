package com.example.demo.meta_data.infrastructure.persistence.entity;

import com.example.demo.meta_data.domain.ExchangeStatus;
import com.example.demo.meta_data.infrastructure.persistence.entity.embeddable.ExchangeKey;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "exchange",
        indexes = {
                @Index(name = "ix_dim_exchange_code", columnList = "code", unique = true),
                @Index(name = "ix_dim_exchange_type", columnList = "exchange_type")
        },
        uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_exchange_business_key",
                columnNames = {"name", "exchange_type", "quote", "country"}
        )
}
        )

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeEntity implements HasMetaKey<ExchangeKey> {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id; // 결정적 ID 직접 세팅

    @Embedded
    private ExchangeKey key;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private ExchangeStatus status = ExchangeStatus.ACTIVE;
}
