package com.example.demo.alert.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "alert_rule")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertRuleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(nullable = false, length = 50)
    private String label;

    @Column(name = "target_type", nullable = false, length = 16)
    private String targetType;

    @Column(name = "asset_symbol", nullable = false, length = 16)
    private String assetSymbol;

    @Column(nullable = false, length = 4)
    private String operator;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal threshold;

    @Column(name = "cooldown_sec", nullable = false)
    private int cooldownSec;

    @Column(nullable = false, length = 64)
    private String channels;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private long createdAt;

    @Column(name = "updated_at", nullable = false)
    private long updatedAt;
}
