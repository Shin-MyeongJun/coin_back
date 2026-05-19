package com.example.demo.alert.watchlist.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "watchlist_item",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_watchlist_item_user_market",
                columnNames = {"user_id", "market_code_id"}
        ),
        indexes = {
                @Index(name = "idx_watchlist_item_user", columnList = "user_id, display_order, id")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchlistItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "market_code_id", nullable = false)
    private long marketCodeId;

    @Column(name = "symbol", length = 32)
    private String symbol;

    @Column(name = "domestic_exchange_id")
    private Long domesticExchangeId;

    @Column(name = "offshore_exchange_id")
    private Long offshoreExchangeId;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "memo", length = 255)
    private String memo;

    @Column(name = "created_at", nullable = false)
    private long createdAt;

    @Column(name = "updated_at", nullable = false)
    private long updatedAt;
}
