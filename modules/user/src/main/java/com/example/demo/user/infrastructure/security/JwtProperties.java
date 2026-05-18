package com.example.demo.user.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;

@ConfigurationProperties("ys.auth.jwt")
public record JwtProperties(
        String secret,
        long accessTtlSeconds,
        long refreshTtlSeconds,
        String issuer
) {
    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("ys.auth.jwt.secret must be configured");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("ys.auth.jwt.secret must be at least 32 bytes for HS256");
        }
        if (accessTtlSeconds <= 0) {
            throw new IllegalArgumentException("ys.auth.jwt.access-ttl-seconds must be > 0");
        }
        if (refreshTtlSeconds <= 0) {
            throw new IllegalArgumentException("ys.auth.jwt.refresh-ttl-seconds must be > 0");
        }
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("ys.auth.jwt.issuer must be configured");
        }
    }
}
