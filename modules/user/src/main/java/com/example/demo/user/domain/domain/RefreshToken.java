package com.example.demo.user.domain.domain;

import java.time.Instant;
import java.util.Objects;

public record RefreshToken(String raw, String jti, AccountId accountId, Instant expiresAt) {
    public RefreshToken {
        Objects.requireNonNull(raw, "RefreshToken.raw must not be null");
        Objects.requireNonNull(jti, "RefreshToken.jti must not be null");
        Objects.requireNonNull(accountId, "RefreshToken.accountId must not be null");
        Objects.requireNonNull(expiresAt, "RefreshToken.expiresAt must not be null");
    }
}
