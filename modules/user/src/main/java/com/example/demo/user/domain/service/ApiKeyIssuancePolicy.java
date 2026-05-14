package com.example.demo.user.domain.service;

import com.example.demo.user.domain.domain.AccountTier;
import com.example.demo.user.domain.domain.ApiKeyIssuanceQuota;
import com.example.demo.user.domain.domain.ApiKeyScope;
import com.example.demo.user.domain.domain.RateLimitPolicies;
import com.example.demo.user.domain.domain.RateLimitPolicy;
import com.example.demo.user.domain.exception.ApiKeyQuotaExceededException;
import com.example.demo.user.domain.exception.ApiKeyScopeNotAllowedException;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public final class ApiKeyIssuancePolicy {

    private ApiKeyIssuancePolicy() {}

    private static final Set<ApiKeyScope> FREE_SCOPES =
            Collections.unmodifiableSet(EnumSet.of(ApiKeyScope.READ_MARKET, ApiKeyScope.SSE_STREAM));
    private static final Set<ApiKeyScope> ALL_SCOPES =
            Collections.unmodifiableSet(EnumSet.allOf(ApiKeyScope.class));

    public static Set<ApiKeyScope> allowedScopes(AccountTier tier) {
        return switch (tier) {
            case FREE -> FREE_SCOPES;
            case PRO, ADMIN -> ALL_SCOPES;
        };
    }

    public static RateLimitPolicy defaultPolicy(AccountTier tier) {
        return RateLimitPolicies.forTier(tier);
    }

    public static int maxKeys(AccountTier tier) {
        return ApiKeyIssuanceQuota.forTier(tier).maxKeys();
    }

    public static void validate(AccountTier tier, Set<ApiKeyScope> requestedScopes, long currentActiveCount) {
        int max = maxKeys(tier);
        if (currentActiveCount >= max) {
            throw new ApiKeyQuotaExceededException(tier, max, currentActiveCount);
        }
        Set<ApiKeyScope> allowed = allowedScopes(tier);
        for (ApiKeyScope s : requestedScopes) {
            if (!allowed.contains(s)) {
                throw new ApiKeyScopeNotAllowedException(tier, s);
            }
        }
    }
}
