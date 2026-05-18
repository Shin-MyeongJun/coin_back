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
@Table(name = "alert_firing")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertFiringEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_id", nullable = false)
    private Long ruleId;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "rule_label", nullable = false, length = 50)
    private String ruleLabel;

    @Column(name = "condition_text", nullable = false, length = 64)
    private String conditionText;

    @Column(name = "observed_value", nullable = false, precision = 20, scale = 8)
    private BigDecimal observedValue;

    @Column(name = "fired_at", nullable = false)
    private long firedAt;
}
