package com.example.demo.user.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "api_key",
        indexes = @Index(name = "ix_api_key_account_id", columnList = "account_id"),
        uniqueConstraints = @UniqueConstraint(name = "uk_api_key_prefix", columnNames = "prefix"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKeyEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "account_id", nullable = false, columnDefinition = "uuid")
    private UUID accountId;

    @Column(name = "label", nullable = false, length = 100)
    private String label;

    @Column(name = "prefix", nullable = false, length = 8)
    private String prefix;

    @Column(name = "hash", nullable = false, length = 100)
    private String hash;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "scopes", nullable = false, columnDefinition = "text[]")
    private String[] scopes;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "ip_allowlist", nullable = false, columnDefinition = "text[]")
    private String[] ipAllowlist;

    @Column(name = "policy_rpm", nullable = false)
    private int policyRpm;

    @Column(name = "policy_rpd", nullable = false)
    private int policyRpd;

    @Column(name = "policy_sse", nullable = false)
    private int policySse;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;
}
