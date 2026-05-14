package com.example.demo.user.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

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
