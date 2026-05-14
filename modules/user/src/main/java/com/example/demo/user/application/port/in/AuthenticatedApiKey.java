package com.example.demo.user.application.port.in;

import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;
import com.example.demo.user.domain.domain.ApiKeyId;
import com.example.demo.user.domain.domain.ApiKeyScope;
import com.example.demo.user.domain.domain.RateLimitPolicy;

import java.util.Objects;
import java.util.Set;

public record AuthenticatedApiKey(
        ApiKeyId apiKeyId,
        AccountId accountId,
        AccountTier tier,
        Set<ApiKeyScope> scopes,
        RateLimitPolicy policy
) {
    public AuthenticatedApiKey {
        Objects.requireNonNull(apiKeyId, "apiKeyId");
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(tier, "tier");
        Objects.requireNonNull(scopes, "scopes");
        Objects.requireNonNull(policy, "policy");
    }
}
