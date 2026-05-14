package com.example.demo.user.application.port.out;

import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;

import java.time.Instant;
import java.util.Objects;

public record VerifiedClaims(AccountId accountId, String jti, AccountTier tier, Instant expiresAt) {
    public VerifiedClaims {
        Objects.requireNonNull(accountId, "VerifiedClaims.accountId must not be null");
        Objects.requireNonNull(jti, "VerifiedClaims.jti must not be null");
        Objects.requireNonNull(tier, "VerifiedClaims.tier must not be null");
        Objects.requireNonNull(expiresAt, "VerifiedClaims.expiresAt must not be null");
    }
}
