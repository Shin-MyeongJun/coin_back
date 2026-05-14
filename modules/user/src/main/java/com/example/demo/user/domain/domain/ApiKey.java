package com.example.demo.user.domain.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiKey {

    private final ApiKeyId id;
    private final AccountId accountId;
    private final String label;
    private final ApiKeyPrefix prefix;
    private final ApiKeyHash hash;
    private final Set<ApiKeyScope> scopes;
    private final Set<String> ipAllowlist;
    private final RateLimitPolicy policy;
    private final Instant createdAt;
    private Instant revokedAt;
    private Instant lastUsedAt;

    public static ApiKey issue(
            ApiKeyId id, AccountId accountId, String label,
            ApiKeyPrefix prefix, ApiKeyHash hash,
            Set<ApiKeyScope> scopes, Set<String> ipAllowlist,
            RateLimitPolicy policy, Instant now
    ) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(prefix, "prefix");
        Objects.requireNonNull(hash, "hash");
        Objects.requireNonNull(scopes, "scopes");
        Objects.requireNonNull(ipAllowlist, "ipAllowlist");
        Objects.requireNonNull(policy, "policy");
        Objects.requireNonNull(now, "now");
        if (label.isBlank()) throw new IllegalArgumentException("label must not be blank");
        if (scopes.isEmpty()) throw new IllegalArgumentException("scopes must not be empty");

        return new ApiKey(
                id, accountId, label,
                prefix, hash,
                copyScopes(scopes), copyIps(ipAllowlist),
                policy, now, null, null
        );
    }

    /**
     * Hydration factory used by persistence/cache mappers — bypasses issue-time invariants
     * because the source of truth is the persisted state.
     */
    public static ApiKey restore(
            ApiKeyId id, AccountId accountId, String label,
            ApiKeyPrefix prefix, ApiKeyHash hash,
            Set<ApiKeyScope> scopes, Set<String> ipAllowlist,
            RateLimitPolicy policy,
            Instant createdAt, Instant revokedAt, Instant lastUsedAt
    ) {
        return new ApiKey(
                id, accountId, label,
                prefix, hash,
                copyScopes(scopes), copyIps(ipAllowlist),
                policy, createdAt, revokedAt, lastUsedAt
        );
    }

    public void revoke(Instant now) {
        Objects.requireNonNull(now, "now");
        if (this.revokedAt == null) this.revokedAt = now;
    }

    public void touchUsage(Instant now) {
        Objects.requireNonNull(now, "now");
        this.lastUsedAt = now;
    }

    public boolean isActive(Instant now) {
        Objects.requireNonNull(now, "now");
        return revokedAt == null;
    }

    public boolean isIpAllowed(String clientIp) {
        if (ipAllowlist == null || ipAllowlist.isEmpty()) return true;
        if (clientIp == null) return false;
        return ipAllowlist.contains(clientIp);
    }

    public boolean hasScope(ApiKeyScope scope) {
        return scopes.contains(scope);
    }

    private static Set<ApiKeyScope> copyScopes(Set<ApiKeyScope> in) {
        if (in.isEmpty()) return Collections.emptySet();
        return Collections.unmodifiableSet(EnumSet.copyOf(in));
    }

    private static Set<String> copyIps(Set<String> in) {
        if (in.isEmpty()) return Collections.emptySet();
        return Collections.unmodifiableSet(new LinkedHashSet<>(in));
    }
}
