package com.example.demo.api.config.security.ssetoken;

import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;
import com.example.demo.user.domain.domain.ApiKeyId;
import com.example.demo.user.domain.domain.ApiKeyScope;
import com.example.demo.user.domain.domain.RateLimitPolicy;

import java.util.Objects;
import java.util.Set;

/**
 * SSE ticket Redis payload.
 *
 * <p>{@code type} 에 따라 jwt 인증 흔적(account)인지 api key 인증 흔적인지 구분한다.
 * Redis 직렬화는 JSON이며 GETDEL 시 1회 소비된다.
 */
public record SseTicketPayload(
        Type type,
        AccountId accountId,
        AccountTier tier,
        ApiKeyId apiKeyId,
        Set<ApiKeyScope> scopes,
        RateLimitPolicy policy
) {

    public enum Type { JWT, API_KEY }

    public SseTicketPayload {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(tier, "tier");
        if (type == Type.API_KEY) {
            Objects.requireNonNull(apiKeyId, "apiKeyId");
            Objects.requireNonNull(scopes, "scopes");
            Objects.requireNonNull(policy, "policy");
            scopes = Set.copyOf(scopes);
        }
    }

    public static SseTicketPayload jwt(AccountId accountId, AccountTier tier) {
        return new SseTicketPayload(Type.JWT, accountId, tier, null, null, null);
    }

    public static SseTicketPayload apiKey(ApiKeyId apiKeyId, AccountId accountId, AccountTier tier,
                                          Set<ApiKeyScope> scopes, RateLimitPolicy policy) {
        return new SseTicketPayload(Type.API_KEY, accountId, tier, apiKeyId, scopes, policy);
    }
}
