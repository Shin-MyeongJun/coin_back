package com.example.demo.api.config.security.filter;

import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;
import com.example.demo.user.domain.domain.ApiKeyId;
import com.example.demo.user.domain.domain.ApiKeyScope;
import com.example.demo.user.domain.domain.RateLimitPolicy;

import java.util.Objects;
import java.util.Set;

public record ApiKeyPrincipal(
        ApiKeyId apiKeyId,
        AccountId accountId,
        AccountTier tier,
        Set<ApiKeyScope> scopes,
        RateLimitPolicy policy
) implements AuthenticationPrincipal {

    public ApiKeyPrincipal {
        Objects.requireNonNull(apiKeyId, "apiKeyId");
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(tier, "tier");
        Objects.requireNonNull(scopes, "scopes");
        Objects.requireNonNull(policy, "policy");
        scopes = Set.copyOf(scopes);
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    public boolean hasScope(ApiKeyScope required) {
        return scopes.contains(required);
    }
}
