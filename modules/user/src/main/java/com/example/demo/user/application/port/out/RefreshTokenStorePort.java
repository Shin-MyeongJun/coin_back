package com.example.demo.user.application.port.out;

import com.example.demo.user.domain.domain.AccountId;

import java.time.Instant;

public interface RefreshTokenStorePort {
    void allow(AccountId accountId, String jti, Instant expiresAt);
    boolean isAllowed(AccountId accountId, String jti);
    void revoke(AccountId accountId, String jti);
    void revokeAll(AccountId accountId);
}
