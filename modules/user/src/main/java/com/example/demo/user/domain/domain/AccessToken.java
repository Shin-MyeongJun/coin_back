package com.example.demo.user.domain.domain;

import java.time.Instant;
import java.util.Objects;

public record AccessToken(String raw, String jti, Instant expiresAt) {
    public AccessToken {
        Objects.requireNonNull(raw, "AccessToken.raw must not be null");
        Objects.requireNonNull(jti, "AccessToken.jti must not be null");
        Objects.requireNonNull(expiresAt, "AccessToken.expiresAt must not be null");
    }
}
