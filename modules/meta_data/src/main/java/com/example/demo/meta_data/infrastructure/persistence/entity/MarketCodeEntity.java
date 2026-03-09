package com.example.demo.meta_data.infrastructure.persistence.entity;

import com.example.demo.meta_data.infrastructure.persistence.entity.embeddable.MarketCodeKey;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "market_code",
        indexes = {
                @Index(name = "ix_dim_market_exchange", columnList = "exchange_id"),
                @Index(name = "ix_dim_market_key", columnList = "exchange_id,market_type,base_asset,quote_asset", unique = true)
        },
        uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_exchange_business_key",
                columnNames = {"trading_pair", "exchange_id", "base", "quote"}
        )
        }
        )
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketCodeEntity implements HasMetaKey<MarketCodeKey>  {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id; // 결정적 ID 직접 세팅

    @Embedded
    private MarketCodeKey key;


}
